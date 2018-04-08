#andlit.fileupload.models.py

from django.db import models

#uploading directory for db files
def db_upload_to(instance, filename):
	return 'files/db/{}/{}'.format(instance.owner_id, filename)

#uploading directory for classifier files
def cl_upload_to(instance, filename):
	return 'files/classifiers/{}/{}'.format(instance.owner_id, filename)

class DBFileModel(models.Model):
	uploaded_file = models.FileField(upload_to=db_upload_to)
	file_hash = models.CharField(max_length=50, default='00')
	owner_id = models.CharField(max_length=50, default='00', null=True)

	#override the delete method
	def delete(self, *args, **kwargs):
		storage, path = self.uploaded_file.storage, self.uploaded_file.path
		super(DBFileModel, self).delete(*args, **kwargs)
		storage.delete(path)

	def __str__(self):
		return self.owner_id

class ClassifierFileModel(models.Model):
	uploaded_file = models.FileField(upload_to=cl_upload_to)
	file_hash = models.CharField(max_length=50, default='00')
	owner_id = models.CharField(max_length=50, default='00', null=True)

	def delete(self, *args, **kwargs):
		storage, path = self.uploaded_file.storage, self.uploaded_file.path
		super(ClassifierFileModel, self).delete(*args, **kwargs)
		storage.delete(path)

	def __str__(self):
		return self.owner_id


