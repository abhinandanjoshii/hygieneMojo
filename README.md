# HygieneMojo

A lightweight open-source Maven plugin that performs project hygiene checks to help developers identify common issues early in the development lifecycle.

## Why HygieneMojo?

Many project issues are small but easy to overlook:

* Missing project documentation
* Missing license files
* SNAPSHOT dependencies in builds
* Accidentally committed large files

HygieneMojo automates these checks and provides quick feedback during development.

## Features

### README Validation

Checks for the existence of a README file.

Supported filenames:

* README.md
* README
* readme.md

### LICENSE Validation

Checks for the existence of a license file.

Supported filenames:

* LICENSE
* LICENSE.txt
* LICENSE.md

### SNAPSHOT Dependency Detection

Detects Maven dependencies that use SNAPSHOT versions.

Example:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>demo-library</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

SNAPSHOT dependencies are often intended for development and may lead to non-reproducible builds.

### Large File Detection

Scans the project directory and reports files that exceed a configurable size threshold.

Ignored directories:

* target
* .git
* .idea

Default threshold:

```text
10 MB
```

## Installation

Build and install the plugin locally:

```bash
mvn clean install
```

## Usage

Run the plugin directly:

```bash
mvn io.github.abhinandanjoshii:hygiene-maven-plugin:0.1.0:check
```

If using a development version:

```bash
mvn io.github.abhinandanjoshii:hygiene-maven-plugin:0.1.0-SNAPSHOT:check
```

## Configuration

### Configure Maximum File Size

Default:

```text
10 MB
```

Override the limit using a Maven property:

```bash
mvn io.github.abhinandanjoshii:hygiene-maven-plugin:0.1.0:check -Dhygiene.maxFileSizeMb=50
```

Example:

```bash
mvn io.github.abhinandanjoshii:hygiene-maven-plugin:0.1.0:check -Dhygiene.maxFileSizeMb=100
```

## Example Output

```text
[INFO] HygieneMojo running

[INFO] README.md found.

[WARNING] None of these files were found [LICENSE, LICENSE.txt, LICENSE.md]

[WARNING] SNAPSHOT dependency detected:
com.example:demo-library:1.0.0-SNAPSHOT

[WARNING] Large file detected:
/project/data/training.csv (120.00 MB)
```

## Roadmap

### Completed

* [x] README validation
* [x] LICENSE validation
* [x] SNAPSHOT dependency detection
* [x] Large file detection
* [x] Configurable file size threshold

### Planned

* [ ] Configurable severity levels
* [ ] Build failure support
* [ ] Additional hygiene rules
* [ ] Maven Central publication
* [ ] Plugin execution through simplified prefix resolution
* [ ] Improved reporting and summary output

## Contributing

Contributions, bug reports, feature requests, and suggestions are welcome.

If you discover an issue or have an idea for a new hygiene rule, feel free to open an issue or submit a pull request.

## Open Source

HygieneMojo is an open-source project maintained on GitHub.

The goal is to provide simple, practical, and extensible Maven project hygiene checks that help developers maintain cleaner and more reliable projects.

## License

MIT License
