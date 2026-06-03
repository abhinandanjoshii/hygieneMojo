# HygieneMojo Plugin - Architecture & Project Plan

## Overview

HygieneMojo is an open-source Maven plugin designed to identify project hygiene, build-quality, and release-readiness issues in Maven projects.

The plugin acts as a quality gate by analyzing project structure, metadata, dependencies, and build configuration before artifacts are released.

### Goal

Provide developers and teams with a simple command to validate project health:

```bash
mvn hygiene:check
```

Example output:

```text
README found ✓
LICENSE found ✓
No SNAPSHOT dependencies ✓
No duplicate dependencies ✓

BUILD SUCCESS
```

or

```text
README missing ⚠
LICENSE missing ✗
SNAPSHOT dependency detected ✗

BUILD FAILED
```

---

# Problem Statement

Many Maven projects accumulate issues that are not caught by the standard build lifecycle:

* Missing documentation
* Missing licensing information
* SNAPSHOT dependencies in release builds
* Missing plugin versions
* Duplicate dependency declarations
* Large accidentally committed files
* Repository hygiene issues

These issues often surface late in the release process and may impact maintainability, reproducibility, legal compliance, and build quality.

HygieneMojo aims to detect these issues early and provide clear, actionable feedback.

---

# Project Objectives

* Provide automated project hygiene validation for Maven projects.
* Support configurable severity levels and validation policies.
* Integrate naturally into existing Maven workflows.
* Produce clear reports suitable for both local development and CI/CD environments.
* Remain lightweight, extensible, and easy to adopt.

---

# Initial Feature Set

## v0.1

Checks:

* README existence
* LICENSE existence

---

## v0.2

Checks:

* SNAPSHOT dependency detection

---

## v0.3

Checks:

* Large file detection

---

## v0.4

Checks:

* Missing plugin version detection

---

## v0.5

Checks:

* Duplicate dependency detection

---

## v1.0

Features:

* Consolidated reporting
* Severity classification
* Configurable validation rules
* CI/CD friendly output
* Stable Maven Central release

---

# Command Interface

Primary goal:

```bash
mvn hygiene:check
```

Future goals:

```bash
mvn hygiene:report
mvn hygiene:validate
```

---

# High-Level Architecture

## Execution Flow

```text
Developer
    |
    v
mvn hygiene:check
    |
    v
HygieneMojo
    |
    v
Build Project Context
    |
    v
Run Checkers
    |
    +--> README Checker
    +--> License Checker
    +--> Snapshot Checker
    +--> Large File Checker
    +--> Plugin Version Checker
    +--> Dependency Checker
    |
    v
Collect Findings
    |
    v
Generate Report
    |
    v
Pass / Fail Build
```

---

# Core Components

## HygieneMojo

Primary plugin entry point.

Responsibilities:

* Receive execution request from Maven.
* Build project context.
* Execute all enabled checkers.
* Aggregate findings.
* Generate final report.
* Determine build outcome.

---

## Project Context

Centralized representation of project information required by checkers.

Examples:

* Project root directory
* pom.xml metadata
* Dependency information
* Plugin information
* Build configuration

The context is created once and shared with all checkers.

---

## Checkers

Each checker is responsible for a single validation rule.

Examples:

### README Checker

Validates:

```text
README.md exists
```

---

### License Checker

Validates:

```text
LICENSE exists
```

---

### Snapshot Checker

Validates:

```text
No SNAPSHOT dependencies exist
```

---

### Large File Checker

Validates:

```text
Repository files remain within configured limits
```

---

### Plugin Version Checker

Validates:

```text
Build plugins define explicit versions
```

---

### Dependency Checker

Validates:

```text
Duplicate dependency declarations are not present
```

---

# Findings Model

Checkers do not print output directly.

Instead, each checker produces findings.

Example:

```text
README missing
Severity: WARNING
```

or

```text
SNAPSHOT dependency detected
Severity: ERROR
```

All findings are collected and reported centrally.

---

# Severity Classification

## INFO

Informational recommendations.

Examples:

* Missing CONTRIBUTORS file
* Suggested repository improvements

---

## WARNING

Issues that should be addressed but do not necessarily block a build.

Examples:

* Missing README
* Large file detected

---

## ERROR

Issues that may impact release quality, legal compliance, or build reproducibility.

Examples:

* Missing LICENSE
* SNAPSHOT dependencies in release builds

---

# Reporting Strategy

All checks execute before the build result is determined.

Workflow:

```text
Run all checks
Collect findings
Generate summary
Determine build outcome
```

Benefits:

* Complete visibility in a single execution
* Reduced iteration cycles
* Improved developer experience

---

# Configuration Strategy

Validation behavior should be configurable through plugin configuration.

Potential configuration options:

* Enabled checks
* Disabled checks
* Maximum file size
* Severity overrides
* Build failure policies

Example:

```xml
<configuration>
    <failOnWarning>false</failOnWarning>
    <failOnError>true</failOnError>
    <maxFileSizeMb>100</maxFileSizeMb>
</configuration>
```

---

# Repository Structure

```text
hygieneMojo
│
├── src
│   ├── main
│   └── test
│
├── docs
│
├── README.md
├── LICENSE
├── ARCHITECTURE.md
└── pom.xml
```

---

# Development Roadmap

## Milestone 1

Project Foundation

Deliverables:

* Repository initialization
* Architecture documentation
* Project roadmap
* Naming and branding decisions

---

## Milestone 2

Plugin Skeleton

Deliverables:

* Maven plugin packaging
* Goal registration
* Successful plugin execution

---

## Milestone 3

Documentation Checks

Deliverables:

* README validation
* LICENSE validation

---

## Milestone 4

Reporting Engine

Deliverables:

* Findings model
* Report generation
* Severity handling

---

## Milestone 5

Dependency Validation

Deliverables:

* SNAPSHOT detection
* Duplicate dependency detection

---

## Milestone 6

Build Configuration Validation

Deliverables:

* Plugin version verification
* Build configuration analysis

---

## Milestone 7

Repository Validation

Deliverables:

* Large file detection
* Additional repository hygiene checks

---

## Milestone 8

Testing & Quality Assurance

Deliverables:

* Integration tests
* Example projects
* Validation scenarios

---

## Milestone 9

CI/CD Integration

Deliverables:

* GitHub Actions workflows
* Automated verification pipeline

---

## Milestone 10

Release

Deliverables:

* Version 1.0.0
* Maven Central publication
* Public documentation

---

# Long-Term Vision

HygieneMojo aims to become a lightweight, extensible project-quality validation tool for Maven ecosystems.

The focus is on helping teams maintain consistent project standards, improve release readiness, and automate repository hygiene checks as part of the build process.
