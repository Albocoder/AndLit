# Andlit API Documentation
This document is to explain andlit api endpoints. Please open an issue for ambigious or wrong information. In order to test the api easily you can use httpie command line tool. For help in getting and using httpie please refer to https://httpie.org/.

## All the package of offered services in postman: 
https://github.com/Albocoder/AndLit/blob/master/server_side/ANDLIT_BACKEND.postman_collection.json

## Content:
1. [User Accounts](#user-accounts)
2. [Vision](#vision)
3. [Image Upload](#image-upload)

## User Accounts:
These endpoints are collected under https://andlit.info/users/ url, and are specific to user related actions.

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

## Vision:
These endpoints are collected under https://andlit.info/vision/ url. Endpoints are here serve our users google-could-vision api 
services that are embedded into our platform.

### Describing an image with labels
This endpoint corresponds to google-cloud-vision api's [label_detection](https://cloud.google.com/vision/docs/detecting-labels) endpoint. By submitting an image users can receive labels, and relative confidence scores in decreasing order for that image. Users can submit images both in raw format, or as a base64 encoded text. In order to do so, they need to submit a POST request to https://andlit.info/vision/describe/ with authorization token and an image. 

The picture below was used for demonstration of this endpoint:

![alt text](sample_images/dollar.jpg)


Sample httpie command for posting image in raw format:
```
http --form --verify=no POST https://andlit.info/vision/describe/ 'Authorization: Token 4f7b3a8a64f19b5fe7ede69d28ed26f084d5301c' image@dollar.jpg
```
Note: for posting image in raw format, provide the relative path to the image after the `@` sign.

Sample httpie command for posting image in base64 encoded format:
```
http --json --verify=no POST https://andlit.info/vision/describe/ 'Authorization: Token 4f7b3a8a64f19b5fe7ede69d28ed26f084d5301c' image="data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2w..."
```
Note: for posting image in base64 encoded format, make sure to include `data:image/jpeg;base64` in the beginning of the image field. Also make sure not to include any newlines for the base64 encoded text. 

Sample response body on successful request:
```
{
    "currency": 0.9420614242553711,
    "money": 0.9182664155960083,
    "banknote": 0.9003868103027344,
    "cash": 0.8480813503265381,
    "paper product": 0.6407275795936584,
    "font": 0.6398560404777527,
    "paper": 0.6072614789009094
}
```
### Retrieving text from an image
This endpoint corresponds to google-cloud-vision api's [document_text_detection](https://cloud.google.com/vision/docs/detecting-fulltext) endpoint. By submitting an image users can receive the full text in the image, alongside with block boundary information. Users can submit images both in raw format, or as a base64 encoded text. In order to do so, they need to submit a POST request to https://andlit.info/vision/read/ with authorization token and an image. 

The picture below was used for demonstration of this endpoint:

![alt text](sample_images/zen.png)


Sample httpie command for posting image in raw format:
```
http --form --verify=no POST https://andlit.info/vision/read/ 'Authorization: Token 4f7b3a8a64f19b5fe7ede69d28ed26f084d5301c' image@zen.png
```
Note: for posting image in raw format, provide the relative path to the image after the `@` sign.

Sample httpie command for posting image in base64 encoded format:
```
http --json --verify=no POST https://andlit.info/vision/read/ 'Authorization: Token 4f7b3a8a64f19b5fe7ede69d28ed26f084d5301c' image="data:image/jpeg;base64,iVBORw0KGgoAAAANSUhEUgAAAhMAAAE..."
```
Note: for posting image in base64 encoded format, make sure to include `data:image/jpeg;base64` in the beginning of the image field. Also make sure not to include any newlines for the base64 encoded text. 

Sample response body on successful request:
```
{
    "block2": {
        "block_text": "Beautiful is better than ugly . Explicit is better than implicit . Simple is better than complex . Complex is better than complicated . Flat is better than nested . Sparse is better than dense . Readability counts . Special cases aren ' t special enough to break the rules . Although practicality beats purity . Errors should never pass silently . Unless explicitly silenced . In the face of ambiguity , refuse the temptation to guess . There should be one - - and preferably only one - - obvious way to do it . Although that way may not be obvious at first unless you ' re Dutch . Now is better than never . Although never is often better than * right * now . If the implementation is hard to explain , it ' s a bad idea . If the implementation is easy to explain , it may be a good idea . Namespaces are one honking great idea - - let ' s do more of those ! \n",
        "block_boundary": "{  \"vertices\": [    {      \"y\": 40,      \"x\": 2    },    {      \"y\": 41,      \"x\": 483    },    {      \"y\": 306,      \"x\": 482    },    {      \"y\": 305,      \"x\": 1    }  ]}"
    },
    "block1": {
        "block_text": "> > > import this The Zen of Python , by Tim Peters \n",
        "block_boundary": "{  \"vertices\": [    {      \"y\": -4,      \"x\": 2    },    {      \"y\": -4,      \"x\": 223    },    {      \"y\": 26,      \"x\": 223    },    {      \"y\": 26,      \"x\": 2    }  ]}"
    }
}
```

## Image Upload
These api endpoints serve services for storing and retrieving image files on the server. 
### Uploading Image
Each user can upload an image file, either in raw format or in base64 encoded form, to the server for backup purposes. To do so user needs to submit a POST request to https://andlit.info/images/upload/ with an image, its MD5 hash code, and their authorization token. Hash code should be unique for the images that user uploads to the server. Upon successfull POST request, details of the image will be returned to the user.

Sample httpie command for uploading image in raw format:
```
http --form --verify=no POST https://andlit.info/images/upload/ 'Authorization: Token 4f7b3a8a64f19b5fe7ede69d28ed26f084d5301c' image@dollar.jpg image_hash='879a176e6d3eeca563e0caabbe000555'
```
Note: for posting image in raw format, provide the relative path to the image after the `@` sign.

Sample httpie command for posting image in base64 encoded format:
```
http --json --verify=no POST https://andlit.info/images/upload/ 'Authorization: Token 4f7b3a8a64f19b5fe7ede69d28ed26f084d5301c' image_hash='879a176e6d3eeca563e0caabbe000555' image="data:image/jpeg;base64,/9j/4AAQSkZJRgABA..."
```
Note: for posting image in base64 encoded format, make sure to include `data:image/jpeg;base64` in the beginning of the image field. Also make sure not to include any newlines for the base64 encoded text. 

Sample response body on successful request:
```
{
    "image": "/media/images/2/050154e2-8b0.jpg",
    "image_hash": "879a176e6d3eeca563e0caabbe000555",
    "pk": 1
}
```

### Retrieving list of images owned by user
By using this endpoint users can get list of the images they have stored on the server. In order to do so, they need to submit a GET request to https://andlit.info/images/list/ with their authorization token.

Sample httpie command for getting the list of images stored in the server:
```
http --form --verify=no GET https://andlit.info/images/list/ 'Authorization: Token 4f7b3a8a64f19b5fe7ede69d28ed26f084d5301c'
``` 

Sample response body on successful request:
```
{
    "image": "https://andlit.info/media/images/2/050154e2-8b0.jpg",
    "image_hash": "879a176e6d3eeca563e0caabbe000555",
    "pk": 1
},
{
    "image": "https://andlit.info/media/images/2/zen.png",
    "image_hash": "f86fc5f9a18eb29cac84f4e0503d2875",
    "pk": 2
}
```

### Retrieving a single image from the server
Users can retrieve the images they have store in the server in a binary format. In order to do so, they need to submit a GET request to https://andlit.info/images/get/ with the image's md5 hash and their authorization token.

Sample httpie command for getting the list of images stored in the server:
```
http --form --verify=no GET https://andlit.info/images/get/ 'Authorization: Token 4f7b3a8a64f19b5fe7ede69d28ed26f084d5301c' image_hash='879a176e6d3eeca563e0caabbe000555'
``` 

Upon successfull request a binary data will be returned to the user. By default httpie does not show this on the console output. In order to save the data, `--output img.jpg` can be added to the beginning of the httpie command, and binary data will be stored in img.jpg file.
