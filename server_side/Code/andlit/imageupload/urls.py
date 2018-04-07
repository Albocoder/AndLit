#andlit.imageupload.url.py

from django.urls import path, include, re_path

from .views import ImageUploadView, ImageUploadListView, ImageUploadRetrieveView

#endpoints for imageupload app in andlit
urlpatterns = [
	path('upload/', ImageUploadView.as_view()),				#endpoint for uploading image
	path('list/', ImageUploadListView.as_view()),			#endpoint for listing the images owned by the user
	path('get/', ImageUploadRetrieveView.as_view()),		#endpoint to get an image
]
