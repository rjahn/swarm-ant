# Overview

This project demonstrates the creation of a WildFly Swarm "fat" jar from an existing war file. The dependencies will be resolved with ivy.

# Usage

This repository contains a full functional Eclipse project. Simply clone the repository and open the project with Eclipse. Add the build.xml to your ANT view and start `create` task.

The project requires jdk 1.8u40 but simply change the JRE in project settings.

The project contains a war file with the name `DemoERP.jar`. This is the demo application of JVx, from sourceforge: http://sourceforge.net/projects/erpdemoapplication/

The jar will be built with `wildfly-swarm-undertow 1.0.0.Alpha4`. If you need additional modules, simply change `ivy.xml` and add e.g:

```xml
<dependency org="org.wildfly.swarm" name="wildfly-swarm-jaxrs" rev="1.0.0.Alpha4" conf="build->default"/>
```
