package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.component.OverheadIndicator.OverheadIndicatorType;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.dialog.QuestDialog;
import com.github.tilcob.game.input.ActiveEntityReference;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.quest.*;
import com.github.tilcob.game.quest.Quest;

import java.util.EnumMap;
import java.util.Map;

public class OverheadIndicatorStateSystem extends IteratingSystem {
    private final Map<NpcRole.Role, OverheadIndicatorType> roleIndicators = new EnumMap<>(NpcRole.Role.class);
    private final Map<String, DialogData> allDialogs;
    private final QuestYarnRegistry questYarnRegistry;
    private final ActiveEntityReference activeEntityReference;
    private ImmutableArray<Entity> players;

    public OverheadIndicatorStateSystem(
        Map<String, DialogData> allDialogs,
        QuestYarnRegistry questYarnRegistry,
        ActiveEntityReference activeEntityReference
    ) {
        super(Family.all(OverheadIndicator.class, NpcRole.class, Npc.class).get());
        this.allDialogs = allDialogs;
        this.questYarnRegistry = questYarnRegistry;
        this.activeEntityReference = activeEntityReference;
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

        OverheadIndicatorType desired = resolveIndicatorType(entity, npcRole, npc);
        if (desired == null) {
            indicator.setVisible(false);
            return;
        }

        indicator.setVisible(true);
        if (desired != indicator.getIndicatorId()) {
            indicator.setIndicatorId(desired);
        }
    }

    private OverheadIndicatorType resolveIndicatorType(Entity entity, NpcRole npcRole, Npc npc) {
        OverheadIndicatorType best = null;
        best = pickHigher(best, resolveRoleIndicator(npcRole));
        best = pickHigher(best, resolveDialogIndicator(entity));
        best = pickHigher(best, resolveQuestIndicator(npc));
        return best;
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

        boolean focused = activeEntityReference != null && activeEntityReference.getFocused() == npcEntity;
        if (!focused) {
            return OverheadIndicatorType.TALK_AVAILABLE;
        }

        if (isPlayerInRange(player, npcEntity)) {
            if (InteractIndicatorSuppression.MAPPER.get(npcEntity) != null) {
                return null;
            }
            return OverheadIndicatorType.INTERACT_HINT;
        }
        return OverheadIndicatorType.TALK_IN_RANGE;
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

        OverheadIndicatorType stepIndicator = resolveQuestStepIndicator(questLog, npc.getName());
        if (stepIndicator != null) {
            return stepIndicator;
        }
        return null;
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
            case DANGER, ANGRY -> 80;
            case MERCHANT -> 70;
            case TALK_BUSY, TALK_CHOICE, INTERACT_HINT, TALK_IN_RANGE, TALK_AVAILABLE, TALKING -> 60;
            default -> 10;
        };
    }

    private Entity getPlayer() {
        if (players == null || players.size() == 0) return null;
        return players.first();
    }
}
