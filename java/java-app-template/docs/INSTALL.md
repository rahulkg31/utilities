# Installation Guide

Install and run the service from the built package (`demo-1.0.0.tar.gz` or `.zip`). For Docker see [README.md](https://claude.ai/README.md); for Kubernetes see [K8S.md](https://claude.ai/chat/K8S.md).

## Prerequisites

- Java 17+ (`java -version`)
- Port 8080 free (or set `SERVER_PORT`)
- Optional: PostgreSQL or MongoDB. The default is a local H2 file database.

## Install

```bash
sha256sum -c demo-1.0.0.tar.gz.sha256    # verify the download
tar -xzf demo-1.0.0.tar.gz -C /opt       # or: unzip demo-1.0.0.zip -d /opt
cd /opt/demo-1.0.0
```

## Configure

The defaults work out of the box. To change settings, edit `conf/application.yml` or set environment variables before starting (full list in [CONFIGURATION.md](https://claude.ai/chat/CONFIGURATION.md)). For example, PostgreSQL:

```bash
export JDBC_TYPE=postgres JDBC_HOST=db.internal JDBC_DATABASE=demo
export JDBC_USERNAME=demo JDBC_PASSWORD='change-me'
```

Start from the install directory so the default H2 data (`./data/`) stays in one place.

## Run

```bash
./bin/start.sh     # Windows: bin\start.bat
./bin/stop.sh      # Windows: bin\stop.bat
```

Check it is up:

```bash
./scripts/healthcheck.sh
# or
curl http://localhost:8080/health
```

## Run as a Linux Service (optional)

```bash
sudo ./scripts/install.sh
sudo systemctl enable --now demo
sudo systemctl status demo
```

## Upgrade

1. Stop the service.
2. Back up `conf/` (and `data/` if using H2).
3. Extract the new version next to the old one.
4. Copy your changes from the old `conf/` into the new one.
5. Start it and run the health check.

## Troubleshooting

| Problem                        | Fix                                                          |
| ------------------------------ | ------------------------------------------------------------ |
| `java: command not found`      | Install Java 17+ or set `JAVA_HOME`.                         |
| `Address already in use`       | Port 8080 is taken; stop the other process or set `SERVER_PORT`. |
| Cannot connect to database     | Check host, port, credentials and TLS settings.              |
| `Permission denied` on scripts | `chmod +x bin/*.sh scripts/*.sh`                             |