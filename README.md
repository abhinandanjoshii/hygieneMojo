# HygieneMojo

Repository governance and build-time policy enforcement for Maven ecosystems.

HygieneMojo is a Maven plugin that codifies repository hygiene, release integrity, and security-adjacent validation rules directly into the build lifecycle. Rather than relying on code review, tribal knowledge, or post-facto CI failures, HygieneMojo surfaces structural and operational risks at the point of build execution.

The objective is simple:

> Shift repository-level failures left, before they become release failures.

---

## Philosophy

Most engineering incidents are not caused by algorithmic defects.

They originate from operational drift:

* unresolved merge artifacts
* credential leakage
* non-deterministic dependency graphs
* repository misconfiguration
* undocumented release assets
* governance violations escaping review

Traditional static analysis validates code.

HygieneMojo validates the repository itself.

---

## Core Capabilities

The current rule set focuses on repository integrity, supply-chain hygiene, and release-readiness validation.

Validation categories include:

* Repository Governance
* Dependency Hygiene
* Source-Control Integrity
* Credential Exposure Detection
* Sensitive Asset Discovery
* Repository Configuration Enforcement

All checks execute locally during Maven execution with no external services, remote APIs, telemetry, or runtime dependencies.

---

## Installation

```xml
<plugin>
    <groupId>io.github.abhinandanjoshii</groupId>
    <artifactId>hygiene-maven-plugin</artifactId>
    <version>0.3.0</version>
</plugin>
```

---

## Execution

```bash
mvn hygiene:check
```

Optional configuration:

```bash
mvn hygiene:check 
```

---

## Architectural Principles

### Policy-As-Code

Repository standards should be executable, not documented.

### Deterministic Execution

Validation must produce consistent outcomes independent of external infrastructure.

### Pre-CI Enforcement

Issues should be detected before pull-request review, CI execution, or release publication.

### Extensible Rule Engine

Every validator is independently composable and intentionally isolated to allow incremental governance expansion without introducing coupling between rule domains.

---

## Roadmap

Future releases will expand HygieneMojo beyond repository hygiene into broader build-governance and release-engineering concerns, including:

* configurable enforcement levels
* build-failure policies
* repository compliance profiles
* conventional commit validation
* release artifact governance
* structured machine-readable reporting
* organization-wide policy packs
* custom rule definitions

---

## Contributing

The project is intentionally designed around isolated validation units.

If you identify a governance gap, recurring repository failure pattern, or operational anti-pattern that should be enforceable at build time, open an Issue describing the use case.

Pull Requests are welcome.

---

## Maven Central

Artifact:

`io.github.abhinandanjoshii:hygiene-maven-plugin`

https://central.sonatype.com/artifact/io.github.abhinandanjoshii/hygiene-maven-plugin

---

## License

MIT License.
