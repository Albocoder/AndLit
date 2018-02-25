from django.urls import path
from . import views
from rest_framework.authtoken import views as framework_views		#framework views

urlpatterns = [
	path('create', views.UserCreate.as_view(), name='user-create'),
	path('changepass', views.UserChangePassword.as_view(), name='user-change-pass'),
	path('getprofile', views.UserGetProfile.as_view(), name='user-get-profile'),
	path('gettoken', framework_views.obtain_auth_token),
]