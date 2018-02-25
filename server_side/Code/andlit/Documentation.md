Documentation for the api endpoints

1. Users endpoints

/users/create
Create a new user by providing username, email, and password
username, and email should be unique, and password should be minimum 8 characters.
Anyone is allowed to register

sample httpie command:
http --json POST http://127.0.0.1:8000/users/api/users "Authorization: Token [token for admin]" username="testuser" email="test@test.com" password="sometestpassword"

/users/changepass
Change the password for the user. Authenticates the user from the token, and 


