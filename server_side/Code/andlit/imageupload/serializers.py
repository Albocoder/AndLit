#andlit.imageupload.serializers.py
from rest_framework import serializers
from .models import ImageUploadModel

from django.core.files.base import ContentFile

import base64
import six
import uuid
import imghdr

#Serializer to convert base64 image data to Imagefield in Django
#works for rest_framework v.3.x
class Base64ImageFieldSerializer(serializers.ImageField):
	def to_internal_value(self, data):
		#Check if input data is base64 string
		if isinstance(data, six.string_types):
			if 'data:' in data and ';base64,' in data:
				header, data = data.split(';base64,')

			#Try to decode the file
			try:
				decoded_file = base64.b64decode(data)
			except TypeError:
				self.fail('invalid_image')

			#Generate file name of 12 chars
			file_name = str(uuid.uuid4())[:12]
			#Get the file name extension
			file_extension = self.get_file_extension(file_name, decoded_file)
			complete_file_name = "%s.%s" % (file_name, file_extension,)
			data = ContentFile(decoded_file, name=complete_file_name)

		return super(Base64ImageFieldSerializer, self).to_internal_value(data)

	def get_file_extension(self, file_name, decoded_file):
		extension = imghdr.what(file_name, decoded_file)
		extension = "jpg" if extension == "jpeg" else extension

		return extension

#serializer for the ImageUploadModel model, users Base64ImageFieldSerializer
class ImageUploadModelSerializer(serializers.HyperlinkedModelSerializer):
	image = Base64ImageFieldSerializer(max_length=None, use_url=True,)
	image_hash = serializers.CharField(max_length=50)

	class Meta:
		model = ImageUploadModel
		fields = ('pk', 'image', 'image_hash')


#simple serializer for image_hash field
class ImageUploadHashSerializer(serializers.Serializer):
	image_hash = serializers.CharField(max_length=50)

