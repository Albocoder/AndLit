#andlit.imageupload.views.py

import os

from django.conf import settings
from django.http import HttpResponse

from rest_framework import status
from rest_framework.response import Response
from rest_framework.generics import CreateAPIView, ListAPIView, RetrieveAPIView

from .serializers import ImageUploadModelSerializer, ImageUploadHashSerializer
from .models import ImageUploadModel

#View for uploading a single image
class ImageUploadView(CreateAPIView):
	serializer_class = ImageUploadModelSerializer

	#upon a post request
	def post(self, request):
		serializer = ImageUploadModelSerializer(data=request.data)			#check the file format for the image

		#if the image being uploaded is valid
		if serializer.is_valid():
			#check whether there is an existing image for the uploader with the same hash
			image_hash = serializer.validated_data.get('image_hash')
			image_set = ImageUploadModel.objects.filter(owner_id=request.user.id)

			for image in image_set:
				if image_hash == image.image_hash:
					#if an image exists for the uploader with the same hash, return an error
					return Response("Hash code for the image file must be unique!", status=status.HTTP_400_BAD_REQUEST)

			#save the image and return the data to user
			serializer.save(owner_id=request.user.id)

			return Response(serializer.data, status=status.HTTP_201_CREATED)

		return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

#View for listing the user's images with details
class ImageUploadListView(ListAPIView):
	serializer_class = ImageUploadModelSerializer

	def get_queryset(self):
		return ImageUploadModel.objects.filter(owner_id=self.request.user.id)

class ImageUploadRetrieveView(RetrieveAPIView):
	serializer_class = ImageUploadHashSerializer

	def get(self, request):
		#check whether the image_hash is valid
		serializer = ImageUploadHashSerializer(data=self.request.data)
		if serializer.is_valid():
			#get the image owned by the user with the submitted image_hash 
			instance = ImageUploadModel.objects.filter(image_hash=serializer.validated_data['image_hash'], owner_id=self.request.user.id)

			if len(instance) == 1:
				image = instance[0].image
				#return the image using Apache's X-sendfile module
				response = HttpResponse()
				response['Content-Type'] = ''
				response['X-Sendfile'] = os.path.join(settings.MEDIA_ROOT, image.name)
				return response

			return Response("Image with submitted hash could not be found", status=status.HTTP_404_NOT_FOUND)
		return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)



