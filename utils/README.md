# Utils

This module contains helper tools and validation logic for the project. The current focus is validating quest and dialog data.

## Contents

- **QuestContentValidator**: validates quest headers, steps, and references to dialog tags.

## Important Gradle tasks

```bash
# Validate quest and dialog data
./gradlew :utils:validateQuestContent
```

The task expects the `assets/` directory as input and validates Yarn quests there.
