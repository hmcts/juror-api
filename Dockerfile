 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.5.3
FROM hmctspublic.azurecr.io/base/java:21-distroless

ENV SPRING_PROFILES_ACTIVE="production"
COPY lib/applicationinsights.json /opt/app/
COPY build/libs/juror-api.jar /opt/app/

EXPOSE 8080
CMD [ "juror-api.jar" ]
