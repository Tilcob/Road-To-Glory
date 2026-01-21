# RewardSystem

## Purpose
Claims quest rewards when a `QuestRewardEvent` is emitted.

## Flow
- Subscribes to `QuestRewardEvent` and calls `questRewardService.claimReward`.

## Key components & events
- QuestRewardEvent
- QuestRewardService
