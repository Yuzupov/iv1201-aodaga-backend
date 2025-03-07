# iv1201-aodaga

the project repo for architecture and design of global applications

# First Section: Installing

## Back-end Server

For the back-end it is required to have Java 17 for development. The back-end uses Maven to build
and it is therefore required to have Maven installed. Navigate to the root directory of the back-end
and run the Maven build command: mvn package. Finally run the main() method in the API class to
start it.

## Database

This application uses PostgreSQL to store data. To initialize the database run the following script:
psql -U postgres -d aodaga -f existing-database.sql

This assumes you have the sql file available on Canvas.

# Deploying changes

To deploy changes it is necessary to push it to the upstream at Heroku. This can be done by doing
the following:
Navigate to the directory that contains the back-end repository.
Enter the following prompt:
$ git push heroku main

# Testing

After each deployment to Heroku the application should be tested immediately in the following
browsers:
Mozilla Firefox, Google Chrome, Microsoft Edge, Safari.

# Architectural Decisions

## Application

Monolithic

## Front-end

MVP (Model-View-Presenter)

## Back-end

MVC (Model-View-Controller) with RESTapi endpoints.

### Validation

A validation method should not return anything. If a validation fails, appropriate Exceptions should
be thrown.

# Languages

## Application

Mixed

## Front-end

JavaScript with React framework

## Back-end

Java with Spark

## Database

PostgreSQL

# Misc

## Build tools

### Back-end

Maven

### Front-end

Vite

## Hosting Service

Heroku

## Documentation

The codebase follows JavaDoc convention of documenting code both in front-end and back-end.
Back-end uses JavaDoc and front-end JSDoc.

## Structure

The code-base follows Google Java convention, in short for the front-end you will use 2-space
indentation

## IDE

Use whichever IDE you want, suggested ones for back-end are IntelliJ and VSCode. For front-end it
matters less but VSCode has live-share so that is something to consider.

## Decisions

Decisions should be unanimous, if unable to reach consensus then a democratic vote should be done.
If there is still a deadlock, keep discussing until either consensus or majority vote is reached.
