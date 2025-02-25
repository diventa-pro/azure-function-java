# Java Based Azure Functions

## Deploy this on Azure

1. Create a resource group
2. Update subscription and RG info in pom.xml
3. `az login`
4. `mvn clean package`
5. `mvn azure-functions:deploy`

## Test this locally

1. Start azurite (e.g. via docker)
   `docker run -p 10000:10000 -p 10001:10001 -p 10002:10002 mcr.microsoft.com/azure-storage/azurite`
2. Copy [example.local.settings.json](example.local.settings.json) to [local.settings.json](local.settings.json)
3. Update [local.settings.json](local.settings.json) according to your env (e.g. Connection Strings)
4. `mvn clean package azure-functions:run`