package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.component.OverheadIndicator.OverheadIndicatorType;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.dialog.QuestDialog;
import com.github.tilcob.game.indicator.IndicatorVisualDef;
import com.github.tilcob.game.indicator.OverheadIndicatorRegistry;
import com.github.tilcob.game.quest.*;
import com.github.tilcob.game.quest.Quest;

import java.util.EnumMap;
import java.util.Map;

public class OverheadIndicatorStateMachineSystem extends IteratingSystem {
    private static final float BOB_AMPLITUDE = 0.15f;
    private static final float BOB_SPEED = 2.5f;
    private static final float PULSE_AMPLITUDE = 0.08f;
    private static final float PULSE_SPEED = 3.0f;
    private static final float FADE_DURATION = 0.2f;

    private final Map<NpcRole.Role, OverheadIndicatorType> roleIndicators = new EnumMap<>(NpcRole.Role.class);
    private final Map<String, DialogData> allDialogs;
    private final QuestYarnRegistry questYarnRegistry;
    private final float showDistanceSquared;
    private final float hideDistanceSquared;
    private ImmutableArray<Entity> players;

    public OverheadIndicatorStateMachineSystem(
        Map<String, DialogData> allDialogs,
        QuestYarnRegistry questYarnRegistry
    ) {
        this(allDialogs, questYarnRegistry,
            Constants.INDICATOR_SHOW_DISTANCE, Constants.INDICATOR_HIDE_DISTANCE);
    }

    public OverheadIndicatorStateMachineSystem(
        Map<String, DialogData> allDialogs,
        QuestYarnRegistry questYarnRegistry,
        float showDistance,
        float hideDistance
    ) {
        super(Family.all(OverheadIndicator.class, Transform.class).get());
        this.allDialogs = allDialogs;
        this.questYarnRegistry = questYarnRegistry;
        this.showDistanceSquared = showDistance * showDistance;
        this.hideDistanceSquared = hideDistance * hideDistance;
        roleIndicators.put(NpcRole.Role.DANGER, OverheadIndicatorType.DANGER);
        roleIndicators.put(NpcRole.Role.MERCHANT, OverheadIndicatorType.MERCHANT);
        roleIndicators.put(NpcRole.Role.QUEST_GIVER, OverheadIndicatorType.QUEST_AVAILABLE);
        roleIndicators.put(NpcRole.Role.INFO, OverheadIndicatorType.INFO);
        roleIndicators.put(NpcRole.Role.TRAINER, OverheadIndicatorType.INFO);
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        players = engine.getEntitiesFor(Family.all(Player.class, Transform.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        OverheadIndicator indicator = OverheadIndicator.MAPPER.get(entity);
        NpcRole npcRole = NpcRole.MAPPER.get(entity);
        Npc npc = Npc.MAPPER.get(entity);

        boolean hasStateDecision = npcRole != null && npc != null;
        OverheadIndicatorType baseType = indicator.getCurrentType();
        if (hasStateDecision) {
            baseType = resolveIndicatorType(entity, npcRole, npc);
            if (baseType != indicator.getCurrentType()) {
                indicator.setCurrentType(baseType);
            }
        }

        OverheadIndicatorType desired = indicator.getDesiredType() != null
            ? indicator.getDesiredType()
            : baseType;
        boolean shouldBeVisible = resolveVisibility(entity, indicator, desired);
        updateState(indicator, desired, shouldBeVisible, deltaTime);
        updateAnimation(indicator, deltaTime);
    }

    private boolean resolveVisibility(
        Entity entity,
        OverheadIndicator indicator,
        OverheadIndicatorType desired) {

        if (desired == null) return false;
        if (desired != OverheadIndicatorType.INTERACT_HINT) return true;

        Entity player = getPlayer();
        if (player == null) return indicator.isVisible();

        Transform playerTransform = Transform.MAPPER.get(player);
        Transform transform = Transform.MAPPER.get(entity);
        if (playerTransform == null || transform == null) return indicator.isVisible();

        float distanceSquared = playerTransform.getPosition().dst2(transform.getPosition());
        boolean inRange =  indicator.isVisible()
            ? distanceSquared < hideDistanceSquared
            : distanceSquared <= showDistanceSquared;

        if (InteractIndicatorSuppression.MAPPER.get(entity) != null) {
            if (!inRange) entity.remove(InteractIndicatorSuppression.class);
            return false;
        }
        return inRange;
    }

    private void updateState(
        OverheadIndicator indicator,
        OverheadIndicatorType desiredType,
        boolean shouldBeVisible,
        float deltaTime
    ) {
        OverheadIndicatorType activeType = indicator.getIndicatorId();
        OverheadIndicator.State state = indicator.getState();

        if (!shouldBeVisible || desiredType == null) {
            if (state != OverheadIndicator.State.FADE_OUT && state != OverheadIndicator.State.HIDDEN) {
                indicator.setState(OverheadIndicator.State.FADE_OUT);
                indicator.setTimer(0f);
            }
        } else if (state == OverheadIndicator.State.HIDDEN) {
            indicator.setIndicatorId(desiredType);
            indicator.setState(OverheadIndicator.State.FADE_IN);
            indicator.setTimer(0f);
        } else if (activeType == null) {
            indicator.setIndicatorId(desiredType);
            indicator.setState(OverheadIndicator.State.FADE_IN);
            indicator.setTimer(0f);
        } else if (activeType != desiredType && state != OverheadIndicator.State.FADE_OUT) {
            indicator.setState(OverheadIndicator.State.FADE_OUT);
            indicator.setTimer(0f);
        }

        state = indicator.getState();
        switch (state) {
            case FADE_IN -> {
                indicator.setVisible(true);
                float timer = indicator.getTimer() + deltaTime;
                float progress = Math.min(timer / FADE_DURATION, 1f);
                indicator.setAlpha(progress);
                indicator.setTimer(timer);
                if (progress >= 1f) {
                    indicator.setAlpha(1f);
                    indicator.setState(OverheadIndicator.State.IDLE);
                    indicator.setTimer(0f);
                }
            }
            case FADE_OUT -> {
                indicator.setVisible(true);
                float timer = indicator.getTimer() + deltaTime;
                float progress = Math.min(timer / FADE_DURATION, 1f);
                indicator.setAlpha(1f - progress);
                indicator.setTimer(timer);
                if (progress >= 1f) {
                    indicator.setAlpha(0f);
                    if (!shouldBeVisible || desiredType == null) {
                        indicator.setVisible(false);
                        indicator.setState(OverheadIndicator.State.HIDDEN);
                    } else {
                        indicator.setIndicatorId(desiredType);
                        indicator.setState(OverheadIndicator.State.FADE_IN);
                        indicator.setTimer(0f);
                    }
                }
            }
            case HIDDEN -> {
                indicator.setVisible(false);
                indicator.setAlpha(0f);
                indicator.setTimer(0f);
            }
            case IDLE, ATTENTION -> {
                indicator.setVisible(true);
                indicator.setAlpha(1f);
            }
        }
    }

    private OverheadIndicatorType resolveIndicatorType(Entity entity, NpcRole npcRole, Npc npc) {
        OverheadIndicatorType best = null;
        best = pickHigher(best, resolveRoleIndicator(npcRole));
        best = pickHigher(best, resolveDialogIndicator(entity));
        best = pickHigher(best, resolveQuestIndicator(npc));
        return best;
    }

    private void updateAnimation(OverheadIndicator indicator, float deltaTime) {
        indicator.setTime(indicator.getTime() + deltaTime);

        IndicatorVisualDef visualDef = OverheadIndicatorRegistry.getVisualDef(indicator.getCurrentType());
        float bobSpeed = visualDef == null ? BOB_SPEED : visualDef.bobSpeed();
        float pulseSpeed = visualDef == null ? PULSE_SPEED : visualDef.pulseSpeed();
        float bobAmplitude = visualDef == null ? BOB_AMPLITUDE : visualDef.bobAmplitude();
        float pulseAmplitude = visualDef == null ? PULSE_AMPLITUDE : visualDef.pulseAmplitude();

        float bobPhase = indicator.getBobPhase() + deltaTime * bobSpeed;
        float pulsePhase = indicator.getPulsePhase() + deltaTime * pulseSpeed;
        indicator.setBobPhase(bobPhase);
        indicator.setPulsePhase(pulsePhase);

        boolean allowBob = indicator.getAllowBob() == null || indicator.getAllowBob();
        boolean allowPulse = indicator.getAllowPulse() == null || indicator.getAllowPulse();

        float currentOffsetY = allowBob ? MathUtils.sin(bobPhase) * bobAmplitude : 0f;
        float currentScale = allowPulse ? 1f + MathUtils.sin(pulsePhase) * pulseAmplitude : 1f;
        indicator.setCurrentOffsetY(currentOffsetY);
        indicator.setScale(currentScale);
    }

    private OverheadIndicatorType resolveRoleIndicator(NpcRole npcRole) {
        if (npcRole == null || npcRole.getRole() == null) {
            return null;
        }
        if (npcRole.getRole() == NpcRole.Role.QUEST_GIVER) {
            return null;
        }
        return roleIndicators.getOrDefault(npcRole.getRole(), OverheadIndicatorType.INFO);
    }

    private OverheadIndicatorType resolveDialogIndicator(Entity npcEntity) {
        Entity player = getPlayer();
        if (player == null) {
            return null;
        }

        Dialog dialog = Dialog.MAPPER.get(npcEntity);
        if (dialog != null && dialog.getState() == Dialog.State.ACTIVE) {
            return OverheadIndicatorType.TALK_BUSY;
        }

        DialogSession dialogSession = DialogSession.MAPPER.get(player);
        if (dialogSession != null && dialogSession.getNpc() == npcEntity && dialogSession.isAwaitingChoice()) {
            return OverheadIndicatorType.TALK_CHOICE;
        }

        Interactable interactable = Interactable.mapper.get(npcEntity);
        if (interactable == null) {
            return null;
        }

        if (isPlayerInRange(player, npcEntity)) {
            if (InteractIndicatorSuppression.MAPPER.get(npcEntity) != null) {
                return null;
            }
            return OverheadIndicatorType.INTERACT_HINT;
        }
        return OverheadIndicatorType.TALK_AVAILABLE;
    }

    private boolean isPlayerInRange(Entity player, Entity target) {
        Transform playerTransform = Transform.MAPPER.get(player);
        Transform targetTransform = Transform.MAPPER.get(target);
        if (playerTransform == null || targetTransform == null) {
            return false;
        }
        float maxDistance = Constants.INDICATOR_SHOW_DISTANCE;
        return playerTransform.getPosition().dst2(targetTransform.getPosition()) <= (maxDistance * maxDistance);
    }

    private OverheadIndicatorType resolveQuestIndicator(Npc npc) {
        Entity player = getPlayer();
        if (player == null || npc == null) return null;
        QuestLog questLog = QuestLog.MAPPER.get(player);
        if (questLog == null) return null;

        QuestDialog questDialog = resolveQuestDialog(npc);
        if (questDialog != null && questDialog.questId() != null && !questDialog.questId().isBlank()) {
            QuestState state = questLog.getQuestStateById(questDialog.questId());
            if (state == QuestState.COMPLETED && isQuestTurnInAvailable(questLog, questDialog.questId())) {
                return OverheadIndicatorType.QUEST_TURNING;
            }
            if (state == QuestState.NOT_STARTED) {
                return OverheadIndicatorType.QUEST_AVAILABLE;
            }
        }

        return resolveQuestStepIndicator(questLog, npc.getName());
    }

    private QuestDialog resolveQuestDialog(Npc npc) {
        if (allDialogs == null || npc == null || npc.getName() == null) return null;
        DialogData dialogData = allDialogs.get(npc.getName());
        return dialogData == null ? null : dialogData.questDialog();
    }

    private boolean isQuestTurnInAvailable(QuestLog questLog, String questId) {
        Quest quest = questLog.getQuestById(questId);
        if (quest == null || quest.isRewardClaimed()) return false;
        QuestDefinition definition = questDefinition(questId);
        return definition == null || definition.rewardTiming() == RewardTiming.GIVER;
    }

    private OverheadIndicatorType resolveQuestStepIndicator(QuestLog questLog, String npcName) {
        if (npcName == null || npcName.isBlank()) return null;
        if (questLog.getQuests().isEmpty()) return null;
        Array<Quest> quests = questLog.getQuests();
        for (int i = 0; i < quests.size; i++) {
            Quest quest = quests.get(i);
            if (quest == null || quest.isCompleted()) {
                continue;
            }
            QuestDefinition definition = questDefinition(quest.getQuestId());
            if (definition == null || definition.steps() == null || definition.steps().isEmpty()) {
                continue;
            }
            int stepIndex = quest.getCurrentStep();
            if (stepIndex < 0 || stepIndex >= definition.steps().size()) {
                continue;
            }
            QuestDefinition.StepDefinition step = definition.steps().get(stepIndex);
            if (step == null || step.npc() == null || step.npc().isBlank()) {
                continue;
            }
            if ("talk".equalsIgnoreCase(step.type()) && npcName.equals(step.npc())) {
                return OverheadIndicatorType.TALK_AVAILABLE;
            }
        }
        return null;
    }

    private QuestDefinition questDefinition(String questId) {
        if (questYarnRegistry == null || questId == null || questId.isBlank()) return null;
        if (questYarnRegistry.isEmpty()) {
            questYarnRegistry.loadAll();
        }
        return questYarnRegistry.getQuestDefinition(questId);
    }

    private OverheadIndicatorType pickHigher(OverheadIndicatorType current, OverheadIndicatorType candidate) {
        if (candidate == null) {
            return current;
        }
        if (current == null || indicatorPriority(candidate) > indicatorPriority(current)) {
            return candidate;
        }
        return current;
    }

    private int indicatorPriority(OverheadIndicatorType type) {
        if (type == null) {
            return 0;
        }
        return switch (type) {
            case QUEST_TURNING -> 100;
            case QUEST_AVAILABLE -> 90;
            case INTERACT_HINT -> 95;
            case DANGER, ANGRY -> 80;
            case MERCHANT -> 70;
            case TALK_BUSY, TALK_CHOICE, TALK_IN_RANGE, TALK_AVAILABLE, TALKING -> 60;
            default -> 10;
        };
    }

    private Entity getPlayer() {
        if (players == null || players.size() == 0) return null;
        return players.first();
    }
}
