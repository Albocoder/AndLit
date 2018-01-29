import cv2
import sys

# Get user supplied values
imagePath = sys.argv[1]
cascPath = "haarcascade_frontalface_default.xml"

# Create the haar cascade
faceCascade = cv2.CascadeClassifier(cascPath)

# Read the image
image = cv2.imread(imagePath)
gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

# Detect faces in the image
faces = faceCascade.detectMultiScale(
    gray,
    scaleFactor=1.1,
    minNeighbors=5,
    minSize=(30, 30),
)

print("Found {0} faces!".format(len(faces)))

# Draw a rectangle around the faces
i = 0
for (x, y, w, h) in faces:
	i = i + 1
	crop_img = image[y:y+h, x:x+w]
	#cv2.imshow("cropped", crop_img)
	#cv2.waitKey(0)
	cv2.imwrite("face"+str(i)+".png",crop_img)

for (x, y, w, h) in faces:
	cv2.rectangle(image, (x, y), (x+w, y+h), (0, 255, 0), 2)

cv2.imshow("Faces found", image)
cv2.waitKey(0)
