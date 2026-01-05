
#!/usr/bin/env bash
set -e

echo "Building modules..."
mvn -q -DskipTests clean package

mkdir -p dist
cp server/target/server.jar dist/server.jar
cp client/target/client.jar dist/client.jar

echo "Done. JARs are in dist/"
