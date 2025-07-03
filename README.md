# GitMergePipeline

GitMergePipeline is a configurable system for setting up rules and conditions to create pipelines that define how Git should merge files together. It can be used as a Git merge driver, a re-merge tool, or a merge tool.

## Features

- Define custom merge pipelines based on file patterns and content
- Apply different merge strategies based on rules
- Use as a Git merge driver, re-merge tool, or merge tool
- Extensible with custom merge operations

## Installation

### Prerequisites

- Java 21 or higher
- Git

### Building from Source

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/GitMergePipeline.git
   cd GitMergePipeline
   ```

2. Build the project:
   ```
   ./gradlew build
   ```

3. Create a distribution:
   ```
   ./gradlew installDist
   ```

The distribution will be available in `build/install/GitMergePipeline`.

## Usage

### Configuration

GitMergePipeline uses a JSON configuration file to define rules and pipelines. The configuration file is searched for in the following locations:

1. The path specified by the `GITMERGEPIPELINE_CONFIG` environment variable
2. `.gitmergepipeline.json` in the current working directory  
3. `.gitmergepipeline.json` in the user's home directory

Example configuration:

```json
{
  "rules": {
    "javaFiles": {
      "type": "filePattern",
      "pattern": "*.java",
      "isRegex": false,
      "caseSensitive": false
    },
    "xmlFiles": {
      "type": "filePattern",
      "pattern": "*.xml",
      "isRegex": false,
      "caseSensitive": false
    }
  },
  "pipelines": [
    {
      "type": "standard",
      "name": "Java Files Pipeline",
      "steps": [
        {
          "rule": {
            "type": "filePattern",
            "pattern": "*.java",
            "isRegex": false,
            "caseSensitive": false
          },
          "operation": "git-merge",
          "parameters": ["recursive"]
        }
      ]
    },
    {
      "type": "fallback",
      "name": "XML Files Fallback Pipeline",
      "steps": [
        {
          "rule": {
            "type": "filePattern",
            "pattern": "*.xml",
            "isRegex": false,
            "caseSensitive": false
          },
          "operation": "git-merge",
          "parameters": ["recursive"]
        },
        {
          "rule": {
            "type": "filePattern",
            "pattern": "*.xml",
            "isRegex": false,
            "caseSensitive": false
          },
          "operation": "git-merge",
          "parameters": ["ours"]
        },
        {
          "rule": {
            "type": "filePattern",
            "pattern": "*.xml",
            "isRegex": false,
            "caseSensitive": false
          },
          "operation": "take-current",
          "parameters": []
        }
      ]
    },
    {
      "type": "conditional",
      "name": "Configuration Files Pipeline",
      "branches": [
        {
          "rule": {
            "type": "filePattern",
            "pattern": "*.properties",
            "isRegex": false,
            "caseSensitive": false
          },
          "pipeline": {
            "type": "standard",
            "name": "Properties Files Pipeline",
            "steps": [
              {
                "operation": "git-merge",
                "parameters": ["recursive"]
              }
            ]
          }
        },
        {
          "rule": {
            "type": "filePattern",
            "pattern": "*.yml",
            "isRegex": false,
            "caseSensitive": false
          },
          "pipeline": {
            "type": "fallback",
            "name": "YAML Files Pipeline",
            "steps": [
              {
                "operation": "git-merge",
                "parameters": ["recursive"]
              },
              {
                "operation": "take-current",
                "parameters": []
              }
            ]
          }
        }
      ],
      "defaultPipeline": {
        "type": "standard",
        "name": "Default Config Pipeline",
        "steps": [
          {
            "operation": "take-other",
            "parameters": []
          }
        ]
      }
    },
    {
      "type": "standard",
      "name": "Default Pipeline",
      "steps": [
        {
          "operation": "git-merge",
          "parameters": ["recursive"]
        }
      ]
    }
  ]
}
```

### Using as a Git Merge Driver

1. Add the following to your `.gitconfig` file:
   ```
   [merge "gitmergepipeline"]
       name = GitMergePipeline Merge Driver
       driver = /path/to/GitMergePipeline/bin/GitMergePipeline driver %O %A %B %P
   ```

2. Add the following to your `.gitattributes` file:
   ```
   *.java merge=gitmergepipeline
   *.xml merge=gitmergepipeline
   ```

### Using as a Re-Merge Tool

To re-merge files that have already been merged:

```
/path/to/GitMergePipeline/bin/GitMergePipeline remerge <base> <current> <other>
```

### Using as a Merge Tool

To use as a merge tool:

```
/path/to/GitMergePipeline/bin/GitMergePipeline tool <local> <remote> <merged>
```

### Using as a `git merge` replacement

In some scenarios such as github actions, you may not be able to get the merge driver to work.  
You can use the `git merge` replacement to run GitMergePipeline as a replacement for `git merge`.  

```
/path/to/GitMergePipeline/bin/GitMergePipeline merge <branch1> <branch2> [branch3 ...] [--base <baseBranch>]
```

## Pipeline Types

GitMergePipeline supports the following pipeline types:

- `standard`: Executes a sequence of merge operations based on rules. If a step fails, the pipeline stops execution and returns the failure result.
- `conditional`: Executes different pipelines based on rules. Each branch in the pipeline has a rule and a pipeline to execute if the rule applies.
- `fallback`: Tries different merge operations until one succeeds. If a step fails, the pipeline continues to the next step instead of stopping.

## Built-in Merge Operations

GitMergePipeline comes with the following built-in merge operations:

- `git-merge`: Uses the Git merge algorithm to merge files
- `take-current`: Uses the current version of the file, ignoring the other version
- `take-other`: Uses the other version of the file, ignoring the current version
- `command-line-merge`: Executes a specified command line to perform the merge

### Using Command Line Merge

The `command-line-merge` operation allows you to specify an external command or script to handle merges. The command can include the following placeholders:

- `%BASE%`: Path to the base version of the file
- `%CURRENT%`: Path to the current version of the file
- `%OTHER%`: Path to the other version of the file
- `%OUTPUT%`: Path to the output file (same as %CURRENT% or mergedPath if available)
- `%FILE%`: Relative path to the file being merged

Example configuration:

```json
{
  "rule": {
    "type": "filePattern",
    "pattern": "*.json",
    "isRegex": false,
    "caseSensitive": false
  },
  "operation": "command-line-merge",
  "parameters": [
    "jq -s '.[0] * .[1]' %CURRENT% %OTHER% > %OUTPUT%",
    "30"
  ]
}
```

The first parameter is the command to execute, and the optional second parameter is the timeout in seconds (default: 60).

## Extending with Custom Merge Operations

You can extend GitMergePipeline with custom merge operations by implementing the `MergeOperation` interface and using the Java ServiceLoader mechanism.

## Extending with Custom Pipelines

You can extend GitMergePipeline with custom pipelines by implementing the `Pipeline` interface and using a Java ServiceLoader mechanism for the `PipelineClassSupplier` interface.

## Extending with Custom Rules

You can extend GitMergePipeline with custom rules by implementing the `Rule` interface and using a Java ServiceLoader mechanism for the `RuleClassSupplier` interface.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
