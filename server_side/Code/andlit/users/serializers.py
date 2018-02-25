from rest_framework import serializers
from rest_framework.validators import UniqueValidator
from django.contrib.auth.models import User

# Serializer for the default user model from django
#serializes 3 fields, email, username, password both ways
class UserSerializer(serializers.ModelSerializer):
	#email should be unique and in proper format
	email = serializers.EmailField(
		required = False,			#required should be true in production
		validators=[UniqueValidator(queryset=User.objects.all())]
		)

	#username should be unique
	username = serializers.CharField(
		max_length=32,
		validators=[UniqueValidator(queryset=User.objects.all())]
		)

	#shortest allowed length is 8 characters for passwords
	password = serializers.CharField(min_length=8, write_only=True)

	#User create method
	def create(self, validated_data):
		user = User.objects.create_user(
			validated_data['username'],
			validated_data['email'],
			validated_data['password']
			)
		return user

	class Meta:
		model = User
		fields = ('id', 'username', 'email', 'password')

#serializer for change password
#has only one field password for the new password
class UserChangePasswordSerializer(serializers.Serializer):
	password = serializers.CharField(min_length=8)

