#!/bin/sh -x
#
#------------------------------------------------------   
#--	BE SURE TO HAVE THE FOLLOWING IN YOUR SETTINGS.XML
#------------------------------------------------------
#
#    <servers>
#        <server>
#            <id>jvnet-nexus-staging</id>
#            <username>jvnet_id</username>
#            <password>password</password>
#        </server>
#    </servers>

# see the following URL for gpg issues
# https://docs.sonatype.org/display/Repository/How+To+Generate+PGP+Signatures+With+Maven#HowToGeneratePGPSignaturesWithMaven-GenerateaKeyPair

mvn --fail-never $1 release:rollback
mvn -B $* release:prepare -DpreparationGoals="install -P\!jvnet-release"
mvn -B $* release:perform -Dgoals="deploy $* -Dgpg.passphrase=glassfish -Pjvnet-release"
