#andlit.imageupload.models.py

from django.db import models

#uploading directory for images
def upload_to(instance, filename):
	return 'images/{}/{}'.format(instance.owner_id, filename)

#ImageUpload model for uploading images to the server.
class ImageUploadModel(models.Model):
	image = models.ImageField("Uploaded Image", upload_to=upload_to)
	owner_id = models.CharField(max_length=50, default='00', null=True)		#uploader's id
	image_hash = models.CharField(max_length=50, default='00')				#hash code for the image
	
	#override the delete method to delete the file from storage
	def delete(self, *args, **kwargs):
		storage, path = self.image.storage, self.image.path
		super(ImageUploadModel, self).delete(*args, **kwargs)
		storage.delete(path)

	def __str__(self):
		return self.image_hash


