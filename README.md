# Cloud Migration project for CSCI-5673 Distributed Systems

- This project evaluated efficiencies gained by relocating CPU cycles to ec2 instances in "Non-Peek" geographical location.

## Components

### boto_scripts
- python scripts for starting, stopping and deleting ec2 servers using boto

### Benchmarking program
- src/com/cloudMigration/BenchmarkVM.java
    + runs SysBench to benchmark cpu

### Migration Services
- src/com/migration/service/MigrationServie.java
    + Web Service for data transport between servers
- src/com/migration/service/client/Scheduler.java
    + Scheduling service that manages when and where to migrate, this program relocated processing by calling boto scripts and initiated the benchmarking program.
