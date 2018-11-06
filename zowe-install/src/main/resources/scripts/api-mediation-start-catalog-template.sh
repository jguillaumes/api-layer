# Variables to be replace:
# - JAVA_SETUP -- sets JAVA_HOME by ZOWE_JAVA_HOME
# - IPADDRESS   -  The IP Address of the system running API Mediation
# - HOSTNAME   -  The hostname of the system running API Mediation (defaults to localhost)
# - DISCOVERY_PORT - The port the discovery service will use
# - CATALOG_PORT - The port the catalog service will use
# - GATEWAY_PORT - The port the gateway service will use

**JAVA_SETUP**
if [[ ":$PATH:" == *":$JAVA_HOME/bin:"* ]]; then
  echo "ZOWE_JAVA_HOME already exists on the PATH"
else
  echo "Appending ZOWE_JAVA_HOME/bin to the PATH..."
  export PATH=$PATH:$JAVA_HOME/bin
  echo "Done."
fi

DIR=`dirname $0`

java -Xms16m -Xmx512m -Dibm.serversocket.recover=true -Dfile.encoding=UTF-8 \
	-Djava.io.tmpdir=/tmp -Xquickstart -Denvironment.hostname=**HOSTNAME** -Denvironment.port=**CATALOG_PORT**  \
	-Denvironment.discoveryLocations=http://eureka:password@**IPADDRESS**:**DISCOVERY_PORT**/eureka/ -Denvironment.ipAddress=**IPADDRESS** \
	-Denvironment.preferIpAddress=true -Denvironment.gatewayHostname=**HOSTNAME** -Denvironment.eurekaUserId=eureka \
	-Denvironment.eurekaPassword=password -Denvironment.truststore=$DIR/../keystore/localhost/localhost.keystore.p12 \
	-Denvironment.truststoreType=PKCS12 -Denvironment.truststorePassword=password \
	-jar $DIR/../api-catalog-services.jar &