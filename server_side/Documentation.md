# Andlit API Documentation
This document is to explain andlit api endpoints. Please open an issue for ambigious or wrong information. In order to test the api easily you can use httpie command line tool. For help in getting and using httpie please refer to https://httpie.org/.

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
This endpoint corresponds to google-cloud-vision api's [label_detection](https://cloud.google.com/vision/docs/detecting-labels) endpoint. By submitting an image users can receive labels, and relative confidence scores for that image. Users can submit images both in raw format, or as a base64 encoded text. In order to do so, they need to submit a POST request to https://andlit.info/vision/describe/ with authorization token and an image. 

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
    "banknote": 0.9003865122795105,
    "cash": 0.8480813503265381,
    "currency": 0.9420613646507263,
    "font": 0.6398560404777527,
    "money": 0.9182664155960083,
    "paper": 0.6072614789009094,
    "paper product": 0.6407275795936584
}
```
### Retrieving text from an image
This endpoint corresponds to google-cloud-vision api's [document_text_detection](https://cloud.google.com/vision/docs/detecting-fulltext) endpoint. By submitting an image users can receive the full text in the image, alongside with block and paragraph boundary information. Users can submit images both in raw format, or as a base64 encoded text. In order to do so, they need to submit a POST request to https://andlit.info/vision/read/ with authorization token and an image. 

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
    "block1": {
        "block_boundary": "{  \"vertices\": [    {      \"x\": 2,      \"y\": -4    },    {      \"x\": 223,      \"y\": -4    },    {      \"x\": 223,      \"y\": 26    },    {      \"x\": 2,      \"y\": 26    }  ]}",
        "paragraph1": {
            "paragraph_boundary": "{  \"vertices\": [    {      \"x\": 2,      \"y\": -4    },    {      \"x\": 223,      \"y\": -4    },    {      \"x\": 223,      \"y\": 26    },    {      \"x\": 2,      \"y\": 26    }  ]}",
            "paragraph_text": "> > > import this The Zen of Python , by Tim Peters "
        }
    },
    "block2": {
        "block_boundary": "{  \"vertices\": [    {      \"x\": 2,      \"y\": 40    },    {      \"x\": 483,      \"y\": 41    },    {      \"x\": 482,      \"y\": 306    },    {      \"x\": 1,      \"y\": 305    }  ]}",
        "paragraph1": {
            "paragraph_boundary": "{  \"vertices\": [    {      \"x\": 2,      \"y\": 40    },    {      \"x\": 483,      \"y\": 41    },    {      \"x\": 482,      \"y\": 306    },    {      \"x\": 1,      \"y\": 305    }  ]}",
            "paragraph_text": "Beautiful is better than ugly . Explicit is better than implicit . Simple is better than complex . Complex is better than complicated . Flat is better than nested . Sparse is better than dense . Readability counts . Special cases aren ' t special enough to break the rules . Although practicality beats purity . Errors should never pass silently . Unless explicitly silenced . In the face of ambiguity , refuse the temptation to guess . There should be one - - and preferably only one - - obvious way to do it . Although that way may not be obvious at first unless you ' re Dutch . Now is better than never . Although never is often better than * right * now . If the implementation is hard to explain , it ' s a bad idea . If the implementation is easy to explain , it may be a good idea . Namespaces are one honking great idea - - let ' s do more of those ! "
        }
    },
    "document_text": ">>> import this\nThe Zen of Python, by Tim Peters\nBeautiful is better than ugly.\nExplicit is better than implicit.\nSimple is better than complex.\nComplex is better than complicated.\nFlat is better than nested.\nSparse is better than dense.\nReadability counts.\nSpecial cases aren't special enough to break the rules.\nAlthough practicality beats purity.\nErrors should never pass silently.\nUnless explicitly silenced.\nIn the face of ambiguity, refuse the temptation to guess.\nThere should be one-- and preferably only one --obvious way to do it.\nAlthough that way may not be obvious at first unless you're Dutch.\nNow is better than never.\nAlthough never is often better than *right* now.\nIf the implementation is hard to explain, it's a bad idea.\nIf the implementation is easy to explain, it may be a good idea.\nNamespaces are one honking great idea -- let's do more of those!\n"
}
```
