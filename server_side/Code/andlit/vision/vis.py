#andlit.vision.vis.py
import io
import os

from google.cloud import vision
from google.cloud.vision import types
from google.protobuf.json_format import MessageToJson

#!!!Authentication for the google-cloud-vision api should be handled in one way or another!!!#

#query an image for label_detection
def describe(image_abs_path):
	#open the image and read content
	with io.open(image_abs_path, 'rb') as image_file:
		content = image_file.read()
	
	#initialize vision api client
	vision_client = vision.ImageAnnotatorClient()
	image = types.Image(content=content)

	#query the image
	response = vision_client.label_detection(image=image)
	labels = response.label_annotations
	
	dictionary = {}
	
	#parse the result for labels, and scores
	for label in labels:
		dictionary[label.description] = label.score
	
	#return the parsed results back
	return dictionary

#query an image for document_text_detection
def read(image_abs_path):
	#open the image and read content
	with io.open(image_abs_path, 'rb') as image_file:
		content = image_file.read()
	
	#initialize vision api client
	vision_client = vision.ImageAnnotatorClient()
	image = types.Image(content=content)

	#query the image
	response = vision_client.document_text_detection(image=image)
	document = response.full_text_annotation
	
	res = {}
	#add the document text to the result
	res['document_text'] = document.text
	
	#parse the result for getting block boundaries, paragraph text and boundaries
	for page in document.pages:
		block_count = 1
		for block in page.blocks:
			block_dict = {}
			block_dict['block_boundary'] = ''.join(MessageToJson(block.bounding_box).split("\n"))
			paragraph_count = 1
			for paragraph in block.paragraphs:
				paragraph_dict = {}
				paragraph_text = ''
				paragraph_dict['paragraph_boundary'] = ''.join(MessageToJson(paragraph.bounding_box).split("\n"))
				for word in paragraph.words:
					for symbol in word.symbols:
						paragraph_text = paragraph_text + symbol.text
					paragraph_text = paragraph_text + ' '
				paragraph_dict['paragraph_text'] = paragraph_text
				paragraph_key = 'paragraph' + str(paragraph_count)
				paragraph_count = paragraph_count + 1
				block_dict[paragraph_key] = paragraph_dict
			block_key = 'block' + str(block_count)
			block_count = block_count + 1
			res[block_key] = block_dict
	#return the result
	return res
