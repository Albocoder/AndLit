#andlit.fileupload.serializers.py

from rest_framework import serializers
from .models import DBFileModel, ClassifierFileModel

class DBFileModelSerializer(serializers.HyperlinkedModelSerializer):
	uploaded_file = serializers.FileField(max_length=None, allow_empty_file=False, use_url=False)
	file_hash = serializers.CharField(max_length=50)

	class Meta:
		model = DBFileModel
		fields = ('pk', 'uploaded_file', 'file_hash')


class ClassifierFileModelSerializer(serializers.HyperlinkedModelSerializer):
	uploaded_file = serializers.FileField(max_length=None, allow_empty_file=False, use_url=False)
	file_hash = serializers.CharField(max_length=50)

	class Meta:
		model = ClassifierFileModel
		fields = ('pk', 'uploaded_file', 'file_hash')


class FileHashSerializer(serializers.Serializer):
	file_hash = serializers.CharField(max_length=50)


