from rest_framework.generics import UpdateAPIView, CreateAPIView, RetrieveAPIView
from rest_framework.response import Response
from rest_framework import status
from .serializers import UserSerializer, UserChangePasswordSerializer
from rest_framework.authtoken.models import Token
from rest_framework.permissions import AllowAny
from django.contrib.auth.models import User

#UserCreate view is for creating user
#permission overridden to allow any user
#uses UserSerializer serializer
class UserCreate(CreateAPIView):
	permission_classes = (AllowAny,)

	def post(self, request, format='json'):
		serializer = UserSerializer(data=request.data)

		#if the received data is valid then proceed
		if serializer.is_valid():
			user = serializer.save()
			#upon success return user information with token
			if user:
				token = Token.objects.get(user=user)
				json = serializer.data
				json['token'] = token.key
				return Response(json, status=status.HTTP_201_CREATED)

		#upon failure return Bad Request
		return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

#UserChangePassword view for updating the user password
#default permission applies
#
class UserChangePassword(UpdateAPIView):
	serializer_class = UserChangePasswordSerializer
	model = User

	def get_object(self, queryset=None):
		obj = self.request.user
		return obj

	def update(self, request, *args, **kwargs):
		self.object = self.get_object()
		serializer = self.get_serializer(data=request.data)

		if serializer.is_valid():
			self.object.set_password(serializer.data.get('password'))
			self.object.save()
			return Response("Changed the password successfully!")

class UserGetProfile(RetrieveAPIView):
	model = User
	serializer_class = UserSerializer

	def get_object(self, queryset=None):
		obj = self.request.user
		return obj

	def get(self, request, *args, **kwargs):
		self.object = self.get_object()
		serializer = self.get_serializer(self.object)
		token = Token.objects.get(user=self.object)
		json = serializer.data
		json['token'] = token.key
		return Response(json)
