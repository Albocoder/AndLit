#andlit.fileupload.views.py

import os

from django.conf import settings
from django.http import HttpResponse

from rest_framework import status
from rest_framework.response import Response
from rest_framework.generics import CreateAPIView, ListAPIView, RetrieveAPIView

from .serializers import DBFileModelSerializer, ClassifierFileModelSerializer, FileHashSerializer
from .models import DBFileModel, ClassifierFileModel

#View for uploading a db file
class DBFileUploadView(CreateAPIView):
	serializer_class = DBFileModelSerializer

	def post(self, request):
		serializer = DBFileModelSerializer(data=request.data)

		if serializer.is_valid():
			file_hash = serializer.validated_data.get('file_hash')
			file_set = DBFileModel.objects.filter(owner_id=request.user.id, file_hash=file_hash)

			if len(file_set) > 0:
				return Response("Hash code for the database file must be unique!", status=status.HTTP_400_BAD_REQUEST)

			serializer.save(owner_id=request.user.id)

			return Response(serializer.data, status=status.HTTP_201_CREATED)

		return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class ClassifierFileUploadView(CreateAPIView):
	serializer_class = ClassifierFileModelSerializer

	def post(self, request):
		serializer = ClassifierFileModelSerializer(data=request.data)

		if serializer.is_valid():
			file_hash = serializer.validated_data.get('file_hash')
			file_set = ClassifierFileModel.objects.filter(owner_id=request.user.id, file_hash=file_hash)

			if len(file_set) > 0:
				return Response("Hash code for the database file must be unique!", status=status.HTTP_400_BAD_REQUEST)

			serializer.save(owner_id=request.user.id)

			return Response(serializer.data, status=status.HTTP_201_CREATED)

		return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

#view for listing stored db files
class DBFileListView(ListAPIView):
	serializer_class = DBFileModelSerializer

	def get_queryset(self):
		return DBFileModel.objects.filter(owner_id=self.request.user.id)


class ClassifierFileListView(ListAPIView):
	serializer_class = ClassifierFileModelSerializer

	def get_queryset(self):
		return ClassifierFileModel.objects.filter(owner_id=self.request.user.id)

#View for retrieving a store db file with its hash
class DBFileRetrieveView(RetrieveAPIView):
	serializer_class = FileHashSerializer

	def get(self, request):
		serializer = FileHashSerializer(data=self.request.data)
		if serializer.is_valid():
			file_hash = serializer.validated_data.get('file_hash')
			file_set = DBFileModel.objects.filter(file_hash=file_hash, owner_id=self.request.user.id)

			if len(file_set) == 1:
				db_file = file_set[0].uploaded_file
				#return the file using Apache's X-SendFile module
				response = HttpResponse()
				response['Content-Type'] = ''
				response['X-Sendfile'] = os.path.join(settings.MEDIA_ROOT, db_file.name)
				return response

			return Response("DB file with submitted hash could not be found", status=status.HTTP_404_NOT_FOUND)

		return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class ClassifierFileRetrieveView(RetrieveAPIView):
	serializer_class = FileHashSerializer

	def get(self, request):
		serializer = FileHashSerializer(data=self.request.data)
		if serializer.is_valid():
			file_hash = serializer.validated_data.get('file_hash')
			file_set = ClassifierFileModel.objects.filter(file_hash=file_hash, owner_id=self.request.user.id)

			if len(file_set) == 1:
				classifier_file = file_set[0].uploaded_file
				#return the file using Apache's X-SendFIle module
				response = HttpResponse()
				response ['Content-Type'] = ''
				response['X-Sendfile'] = os.path.join(settings.MEDIA_ROOT, classifier_file.name)
				return response

			return Response("Classifier file with submitted hash could not be found", status=status.HTTP_404_NOT_FOUND)

		return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)



