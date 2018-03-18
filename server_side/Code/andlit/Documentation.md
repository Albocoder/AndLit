# Andlit API Documentation
This document is to explain andlit api endpoints. Please open an issue for ambigious or wrong information. In order to test the api easily you can use httpie command line tool. For help in getting and using httpie please refer to https://httpie.org/.

## User Accounts:

### Creating A User:
In order to use any of the api services, first a user account is needed to be created on our platform. You can do so by submitting a POST request to https://andlit.info/users/create providing a username, email, and password. Username and email are required to be unique, and password should not be less than 8 characters. Upon successful request, username, email, id, and authorization token will be returned in the response. The authorization token should be submitted in the following requests for authentication.

Sample httpie command:
```
http --verify=no --json POST https://andlit.info/users/create username='mamed' email='mamed@mamed.com' password='somesuperpasswordhere'
```
Sample response body on successful request:
```
{
    "email": "mamed@mamed.com",
    "id": 2,
    "token": "4f7b3a8a64f19b5fe7ede69d28ed26f084d5301c",
    "username": "mamed"
}
```
### Getting User Information:
In order to get user information, users need to submit a GET request to https://andlit.info/users/getprofile, with their authorization token. Upon successful request, username, email, id, and authorization token will be returned in the response.

Sample httpie command:
```
http --verify=no --json GET https://andlit.info/users/getprofile 'Authorization: Token 4f7b3a8a64f19b5fe7ede69d28ed26f084d5301c'
```
Sample response body on successful request:
```
{
    "email": "mamed@mamed.com",
    "id": 2,
    "token": "4f7b3a8a64f19b5fe7ede69d28ed26f084d5301c",
    "username": "mamed"
}
```
### Changing User Password
In order to change user password, users need to submit a PUT request to https://andlit.info/users/changepass with authorization token and password for their new password. 

Sample httpie command:
```
http --verify=no --json PUT https://andlit.info/users/changepass 'Authorization: Token 4f7b3a8a64f19b5fe7ede69d28ed26f084d5301c' password='superduperpasswordhere'
```
Sample response body on successful request:
```
"Changed the password successfully!"
```
### Retrieving Authentication Token
In order to retrieve authentication token for the user, users need to submit a POST request to https://andlit.info/users/gettoken with username and password. Upon successful request, authorization token will be returned in the response.

Sample httpie command:
```
http --verify=no --json POST https://andlit.info/users/gettoken username='mamed' password='superduperpasswordhere'
```
Sample response body on successful request:
```
{
    "token": "4f7b3a8a64f19b5fe7ede69d28ed26f084d5301c"
}
```
