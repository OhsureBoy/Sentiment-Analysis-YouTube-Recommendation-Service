import openai
from flask import Flask, request, jsonify
from flask_cors import CORS
import requests
import re

app = Flask(__name__)
CORS(app)

openai.api_key = 'openai 키를 여기다 입력하세요'
youtube_api_key = 'youtubeKey를 여기다 입력하세요'

# def extract_search_keywords(text):
#     keyword_match = re.search(r'키워드: (.+)', text)
#     if keyword_match:
#         return keyword_match.group(1).split(", ")
#     return []

def extract_emotion_and_keywords(text):
    prompt = f"""
    다음 예제들을 보고 유사한 입력에 대해 심리상태와 감정을 판단하고, 문장에서 주제와 직접적으로 관련된 키워드들만 추출하세요.

    예제 1: 
    입력: 제 성별은 여성입니다. 제 나이는 25세입니다. 최근 일이 너무 힘들고 스트레스를 많이 받고 있어요. 매일 밤 잠을 이루지 못해요.
    출력: 심리상태: 스트레스, 불안 / 감정: 피로, 걱정 / 키워드: 일, 스트레스, 잠

    예제 2:
    입력: 제 성별은 남성입니다. 제 나이는 30세입니다. 최근 운동을 시작했는데 기분이 좋아지고 있어요. 에너지가 넘치는 느낌이에요.
    출력: 심리상태: 긍정적, 활기참 / 감정: 행복, 활력 / 키워드: 운동, 기분, 에너지

    예제 3:
    입력: 제 성별은 여성입니다. 제 나이는 40세입니다. 최근 가족과의 갈등으로 많이 우울하고 힘들어요.
    출력: 심리상태: 우울, 스트레스 / 감정: 슬픔, 피로 / 키워드: 가족, 갈등, 우울  

    예제 4:
    입력: 제 성별은 남성입니다. 제 나이는 22세입니다. 최근에 친구들과 함께 여행을 다녀왔는데 너무 즐거웠어요. 새로운 경험도 많이 했고 좋은 추억이 되었어요.
    출력: 심리상태: 행복, 만족 / 감정: 즐거움, 활기참 / 키워드: 친구, 여행, 경험, 추억

    입력: {text}
    """
    response = openai.Completion.create(
        engine="gpt-3.5-turbo-instruct",
        prompt=prompt,
        max_tokens=150
    )
    return response.choices[0].text.strip()

def extract_emotion_and_keywords_without_heart_rate(text):
    prompt = f"""
    다음 예제들을 보고 유사한 입력에 대해 심리상태와 감정을 판단하고, 문장에서 주제와 직접적으로 관련된 키워드들만 추출하세요.

    예제 1: 
    입력: 현재 심박수는 85. 제 성별은 여성입니다. 제 나이는 25세입니다. 최근 일이 너무 힘들고 스트레스를 많이 받고 있어요. 매일 밤 잠을 이루지 못해요.
    출력: 심리상태: 스트레스, 불안 / 감정: 피로, 걱정 / 키워드: 일, 스트레스, 잠

    예제 2:
    입력: 현재 심박수는 114. 제 성별은 남성입니다. 제 나이는 30세입니다. 최근 운동을 시작했는데 기분이 좋아지고 있어요. 에너지가 넘치는 느낌이에요.
    출력: 심리상태: 긍정적, 활기참 / 감정: 행복, 활력 / 키워드: 운동, 기분, 에너지

    예제 3:
    입력: 현재 심박수는 80. 제 성별은 여성입니다. 제 나이는 40세입니다. 최근 가족과의 갈등으로 많이 우울하고 힘들어요.
    출력: 심리상태: 우울, 스트레스 / 감정: 슬픔, 피로 / 키워드: 가족, 갈등, 우울  

    예제 4:
    입력: 현재 심박수는 90. 제 성별은 남성입니다. 제 나이는 22세입니다. 최근에 친구들과 함께 여행을 다녀왔는데 너무 즐거웠어요. 새로운 경험도 많이 했고 좋은 추억이 되었어요.
    출력: 심리상태: 행복, 만족 / 감정: 즐거움, 활기참 / 키워드: 친구, 여행, 경험, 추억

    입력: {text}
        """
    response = openai.Completion.create(
        engine="gpt-3.5-turbo-instruct",
        prompt=prompt,
        max_tokens=150
    )
    return response.choices[0].text.strip()

def recommend_youtube_videos(keywords):
    print(keywords)
    search_url = "https://www.googleapis.com/youtube/v3/search"
    params = {
        'part': 'snippet',
        'q': keywords,
        'key': youtube_api_key,
        'order': 'relevance',  # 관련성 순으로 정렬
        'maxResults': 8
    }
    response = requests.get(search_url, params=params)
    video_data = response.json()
    return video_data.get('items', []) 

@app.route('/analyze_without_heart_rate', methods=['POST'])
def analyze_without_heart_rate():
    data = request.json
    user_text = data['text']
    age_text = data['age']
    gender_text = data['gender']
    heart_rate = data['heart_rate']
    combined_text = f"현재 심박수는 {heart_rate}. 제 성별은 {gender_text}. 제 나이는 {age_text}. {user_text}"
    emotions_keywords = extract_emotion_and_keywords_without_heart_rate(combined_text)
    # search_emotion_keywords = extract_search_keywords(emotions_keywords)
    recommended_videos = recommend_youtube_videos(emotions_keywords)

    videos = []
    for video in recommended_videos:
        video_id = video.get('id', {}).get('videoId')
        videos.append({
            'title': video['snippet']['title'],
            'url': f"https://www.youtube.com/watch?v={video_id}",
            'thumbnail_url': video['snippet']['thumbnails']['high']['url']
        })
    response_data = {'emotions_keywords': emotions_keywords, 'recommended_videos': videos}
    print(response_data)
    return jsonify(response_data)

@app.route('/analyze', methods=['POST'])
def analyze():
    data = request.json
    print(data)
    heart_rate = data['heart_rate']
    user_text = data['text']
    age_text = data['age']
    gender_text = data['gender']
    combined_text = f"제 성별은 {gender_text}. 제 나이는 {age_text}. {user_text}"
    emotions_keywords = extract_emotion_and_keywords(combined_text)
    # search_emotion_keywords = extract_search_keywords(emotions_keywords)
    recommended_videos = recommend_youtube_videos(emotions_keywords)

    videos = []
    for video in recommended_videos:
        video_id = video.get('id', {}).get('videoId')
        videos.append({
            'title': video['snippet']['title'],
            'url': f"https://www.youtube.com/watch?v={video_id}",
            'thumbnail_url': video['snippet']['thumbnails']['high']['url']
        })

    response_data = {'emotions_keywords': emotions_keywords, 'recommended_videos': videos}
    print(response_data)
    return jsonify(response_data)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
