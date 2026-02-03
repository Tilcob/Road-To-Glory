# Utils

This module contains helper tools and validation logic for the project. The current focus is validating quest and dialog data.

## Contents

- **QuestContentValidator**: validates quest headers, steps, and references to dialog tags.

## Important Gradle tasks

```bash
# Validate quest and dialog data
./gradlew :utils:validateQuestContent

# Run tests plus content validation
./gradlew check
```

The validation task expects the `assets/` directory as input and validates Yarn quests there. The `check` task in the root project includes this validation step.
