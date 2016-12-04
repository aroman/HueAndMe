import os
from pymongo import MongoClient
from flask import Flask, request, jsonify
app = Flask(__name__)

PORT = os.getenv('PORT', 8000)
MONGO_URI = os.getenv('MONGO_URI')

scores = {
    'avi': 420,
    'foo': 410,
    'lol': 500,
}

client = MongoClient(MONGO_URI)
db = client['hue-and-me']

@app.route('/')
def index():
    return 'nothing to see here :^)'

@app.route('/scores', methods=['GET', 'POST'])
def scores():
    if request.method == 'POST':
        return postScore(request)
    return getScores(request)

def getScores(request):
    scores = []
    for score in db.scores.find().sort('points', -1):
        scores.append(dict(name=score['name'], points=score['points']))
    response = jsonify(scores=scores)
    return response

def postScore(request):
    content = request.get_json(force=True)
    if 'name' not in content:
        return statusResponse('`name` field missing', 400)
    if 'points' not in content:
        return statusResponse('`points` field missing', 400)
    if (not content['points'].isdigit()) or (int(content['points']) < 0):
        return statusResponse('`points` field must be a non-negative integer', 400)
    score = dict(name=content['name'], points=int(content['points']))
    db.scores.insert_one(score)
    return statusResponse('success')

def statusResponse(text, status_code=200):
    response = jsonify(statusText=text)
    response.status_code = status_code
    return response

if __name__ == '__main__':
    app.run(port=PORT, debug=True)
