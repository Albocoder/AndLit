#andlit.vision.models.py
from django.db import models

#uploading directory
#upload to images/vision/
def upload_to(instance, filename):
	return 'images/{}/{}'.format("vision", filename)

#Image model for vision
class VisionImageUploadModel(models.Model):
	image = models.ImageField("Uploaded Image", upload_to=upload_to)
	
	#override delete to delete the file locally from the storage as well
	def delete(self, *args, **kwargs):
		storage, path = self.image.storage, self.image.path
		super(VisionImageUploadModel, self).delete(*args, **kwargs)
		storage.delete(path)

	def __str__(self):
		return self.image
