#andlit.vision.url.py

from django.urls import path, include
from rest_framework.urlpatterns import format_suffix_patterns

from .views import VisionDescribeView, VisionReadView

#endpoints for vision app in andlit
urlpatterns = [
    path('describe/', VisionDescribeView.as_view()),
    path('read/', VisionReadView.as_view()),
]

