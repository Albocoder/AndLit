#andlit.vision.views.py
from django.conf import settings

from .serializers import VisionImageUploadModelSerializer

from rest_framework import status
from rest_framework.response import Response
from rest_framework.generics import CreateAPIView, UpdateAPIView

#import methods for google-cloud-vision-api
from . import vis

#View endpoint for document_text_detection from google-cloud-vision-api
class VisionReadView(CreateAPIView):	
	serializer_class = VisionImageUploadModelSerializer
	
	#override the post method
	def post(self, request):
		serializer = VisionImageUploadModelSerializer(data=request.data)
		#check whether an image field was submitted
		#can be both raw image file or base64 decoded
		if serializer.is_valid():
			instance = serializer.save()	#save the image locally
			image_abs_path = instance.image.path	#get the image path in the local storage
			
			#query the image
			result = vis.read(image_abs_path)

			instance.delete()	#delete the instance from local storage
			
			#return the result upon success
			return Response(result, status=status.HTTP_200_OK)

		#return the errors upon failure
		return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

#View endpoint for label_detection from google-cloud-vision-api
class VisionDescribeView(CreateAPIView):
	serializer_class = VisionImageUploadModelSerializer
	
	#override the post method
	def post(self, request):
		serializer = VisionImageUploadModelSerializer(data=request.data)
		
		#check whether an image field was submitted
		#can be both raw image file or base64 decoded
		if serializer.is_valid():
			instance = serializer.save()	#save the image locally
			image_abs_path = instance.image.path	#get the image path in the local storage
			
			#query the image
			labels = vis.describe(image_abs_path)
			
			instance.delete()	#delete the instance from local storage
			
			#return the result upon success
			return Response(labels, status=status.HTTP_200_OK)

		#return the errors upon failure
		return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


