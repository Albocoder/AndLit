#andlit.fileupload.urls.py

from django.urls import path, include

from .views import DBFileUploadView, DBFileListView, DBFileRetrieveView, ClassifierFileUploadView, ClassifierFileListView, ClassifierFileRetrieveView

#endpoints for the fileupload app in andlit
urlpatterns = [
	path('db/upload/', DBFileUploadView.as_view()),
	path('db/list/', DBFileListView.as_view()),
	path('db/get/', DBFileRetrieveView.as_view()),
	path('cl/upload/', ClassifierFileUploadView.as_view()),
	path('cl/list/', ClassifierFileListView.as_view()),
	path('cl/get/', ClassifierFileRetrieveView.as_view()),
]
