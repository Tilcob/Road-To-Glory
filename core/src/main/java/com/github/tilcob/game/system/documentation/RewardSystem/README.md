# RewardSystem

## Zweck
Beansprucht Quest-Belohnungen, sobald ein QuestRewardEvent ausgelöst wird.

## Ablauf
- Subscribt QuestRewardEvent und ruft questRewardService.claimReward auf.

## Wichtige Komponenten & Ereignisse
- QuestRewardEvent
- QuestRewardService
