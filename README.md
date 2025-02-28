# iv1201-aodaga
the project repo for architecture and design of global applications

# First Section: Installing
## Back-end Server
For the back-end it is required to have Java 17 for development. The back-end uses Maven to build and it is therefore required to have Maven installed. Navigate to the root directory of the back-end and run the Maven build command: mvn package. Finally run the main() method in the API class to start it.

## Database
This application uses PostgreSQL to store data. To initialize the database run the following script:
psql -U postgres -d aodaga -f existing-database.sql

This assumes you have the sql file available on Canvas.

# Deploying changes
To deploy changes it is necessary to push it to the upstream at Heroku. This can be done by doing the following:
Navigate to the directory that contains the back-end repository.
Enter the following prompt:
$ git push heroku main

# Testing
After each deployment to Heroku the application should be tested immediately in the following browsers:
Mozilla Firefox, Google Chrome, Microsoft Edge, Safari.

# Architectural Decisions
## Application
Monolithic
## Front-end
MVP (Model-View-Presenter)
## Back-end
MVC (Model-View-Controller) with RESTapi endpoints.

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
The code-base follows Google Java convention, in short for the front-end you will use 2-space indentation
## IDE
Use whichever IDE you want, suggested ones for back-end are IntelliJ and VSCode. For front-end it matters less but VSCode has live-share so that is something to consider.
## Decisions
Decisions should be unanimous, if unable to reach consensus then a democratic vote should be done. If there is still a deadlock, keep discussing until either consensus or majority vote is reached.

# Backend API documentation
## note on encrypted endpoints
In communication we use encryption between the frontend and the backend.
Every request subsequently listed will take a json object encrypted and wrapped inside a encrypted endpoint request json.

Successful responses will also be json objects wrapped in an encrypted response json.

<details><summary><code>encrypted endpoint communication format</code></summary>

##### Encrypted endpoint request
> | name        |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | cipher      |  required | string   | The encrypted json data, encrypted with the symmetric key  |
> | iv          |  required | string   | The initialization vector                                  |
> | key         |  required | string   | the symmetric key, encrypted with the public key           |
> | timestamp   |  required | string   | timestamp string, will be sent back signed as a response   |

##### Encrypted endpoint response
> | name        |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | cipher      |  required | string   | The encrypted JSON data, encrypted with the symmetric key obtained in the request|
> | iv          |  required | string   | The initialization vector                                  |
> | signature   |  required | string   | timestamp string, signed with the private key and encrypted with the symmetric key  |
> | signature_iv          |  required | string   | The initialization vector                                  |
</details>




------------------------------------------------------------------------------------------

#### something

<details>
 <summary><code>POST</code> <code><b>/login</b></code> <code>Log in user</code></summary>

##### Parameters

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | username     | optional | string | one of username or userEmail is required, if both is provided username takes precedence  |
> | userEmail    | optional | string | see 'username' |
> | userPassword | required | string |   |


##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | see below                                |
> | `400`         | `text/html;charset=utf-8`         | `Bad request, error message`                       |
> | `403`         | `text/html;charset=utf-8`         | `Bad credentials`                                  |

###### 200 response object

> | name      |  data type               | description                                                           |
> |-----------|--------------------------|-----------------------------------------------------------------------|
> | token     | string | token, encrypted with private key, sent back in requests to prove identity |
> | expirationDate | number | timestamp(seconds) of the tokens expiration time/date |
> | username  | string |  |
> | userEmail | string |  |
> | role      | string | eg `recruiter` or `applicant` |


</details>

<details>
 <summary><code>POST</code> <code><b>/register</b></code> <code>Register as new applicant</code></summary>

##### Parameters

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | firstName      |  required | string   | N/A  |
> | lastName       |  required | string   | N/A  |
> | personalNumber |  required | string   | "xxxxxxxx-xxxx"  |
> | email          |  required | string   | N/A  |
> | userPassword   |  required | string   | N/A  |
> | username       |  required | string   | N/A  |


##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | `empty JSON object`                                |
> | `400`         | `text/html;charset=utf-8`         | `Bad request, error message`                       |


</details>

#### recruiter actions

<details>
 <summary><code>POST</code> <code><b>/applicants</b></code> <code>list all applicants</code></summary>

##### Parameters

empty JSON object

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | token      |  required | string   | the token obtained when logging in  |


##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | {applicants: [{applicant object, see below}]}                                  |
> | `400`         | `text/html;charset=utf-8`         | `Bad request, error message`               |
> | `403`         | `text/html;charset=utf-8`         | `User not allowed to perform this action`  |

###### applicant object

> | name      |  data type               | description                                                           |
> |-----------|--------------------------|-----------------------------------------------------------------------|
> | name  | string |  |
> | surname | string |  |
> | status      | string | eg `unhadled`, `accepted` or `rejected` |
> | availabilities | list[availability] | see below |

##### availability object
> | name      |  data type               | description                                                           |
> |-----------|--------------------------|-----------------------------------------------------------------------|
> | from  | string | date formatted string |
> | to    | string | date formatted string |





</details>
<details>
 <summary><code>POST</code> <code><b>/applicats/update</b></code> <code>update an applicant (not fully implemented)</code></summary>

##### Parameters

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | token      |  required | string   | the token obtained when logging in  |
> | password   |  required | string   | the password for the user who owns the token  |
> | applicantEmail  |  required | string   | NOT IMPLEMENTED  |
> | applicantStatus |  required | string   | NOT IMPLEMENTED  |


##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | `empty JSON object`                                |
> | `400`         | `application/json`                | `Bad Request`                            |
> | `403`         | `text/html;charset=utf-8`         | `not allowed or bad credentials`                                                               |
</details>

#### reset lost password
<details>
 <summary><code>POST</code> <code><b>/password-reset</b></code> <code>reset password using reset-password-link</code></summary>

##### Parameters

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | link      |  required | string   | the password-reset-link  |
> | password  |  required | string   | the new password  |


##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | `empty JSON object`                                |
> | `400`         | `application/json`                | `Bad Request`                            |


</details>
<details>
 <summary><code>POST</code> <code><b>/password-reset/create-link</b></code> <code>create and obtain a reset-password-link (sent via email)</code></summary>

##### Parameters

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | email      |  required | string   | the email of the user to create password-reset-link for  |


##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | `empty JSON object`                                |
> | `400`         | `application/json`                | `Bad Request`                            |

</details>

<details>
 <summary><code>POST</code> <code><b>/password-reset/validate-link</b></code> <code>validate wether password-reset-link exists and is not expired</code></summary>

##### Parameters

> | name      |  type     | data type               | description                                                           |
> |-----------|-----------|-------------------------|-----------------------------------------------------------------------|
> | link      |  required | string   | the password-reset-link to validate |


##### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | `empty JSON object`                                |
> | `400`         | `application/json`                | `Bad Request`                            |

</details>





------------------------------------------------------------------------------------------
