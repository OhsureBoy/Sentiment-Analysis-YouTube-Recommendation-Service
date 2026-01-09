import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import './App.css';

function App() {
  const [text, setText] = useState('');
  const [age, setAge] = useState('');
  const [gender, setGender] = useState('');
  const [messages, setMessages] = useState([]);
  const [error, setError] = useState(null);
  const messagesEndRef = useRef(null);

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      const userMessage = { type: 'user', text, age, gender };
      setMessages((prevMessages) => [...prevMessages, userMessage]);
  
      const result = await axios.post('http://175.197.91.11:1133/analyze', {
        text,
        heart_rate: 70,  // 예시 값입니다. 실제 데이터로 교체하세요.
        age: parseInt(age), // 나이를 정수로 변환
        gender
      });
  
      const botMessage = {
        type: 'bot',
        emotions_keywords: result.data.emotions_keywords,
        recommended_videos: result.data.recommended_videos,
      };
      setMessages((prevMessages) => [...prevMessages, botMessage]);
      setText(''); // 텍스트 필드만 초기화
    } catch (error) {
      setError('분석 중 오류가 발생했습니다.');
    }
  };

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  return (
    <div className="App">
      <header className="App-header">
        <h1>감정 분석 및 유튜브 추천 서비스</h1>
      </header>
      <main className="chat-container">
        <div className="messages">
          {messages.map((message, index) =>
            message.type === 'user' ? (
              <div key={index} className="message user-message">
                <p>{message.text}</p>
                <small>나이: {message.age}, 성별: {message.gender}</small>
              </div>
            ) : (
              <div key={index} className="message bot-message">
                <p>감정 및 키워드: {message.emotions_keywords}</p>
                <h3>추천 유튜브 비디오</h3>
                <ul>
                  {message.recommended_videos.map((video, vidIndex) => (
                    <li key={vidIndex}>
                      <a href={video.url} target="_blank" rel="noopener noreferrer">
                        <img src={video.thumbnail_url} alt={video.title} />
                        <p>{video.title}</p>
                      </a>
                    </li>
                  ))}
                </ul>
              </div>
            )
          )}
          <div ref={messagesEndRef} />
        </div>
        <form onSubmit={handleSubmit} className="input-form">
          <textarea
            value={text}
            onChange={(e) => setText(e.target.value)}
            placeholder="현재 하고 싶은 말을 적어주세요"
            required
          />
          <input
            type="number"
            value={age}
            onChange={(e) => setAge(e.target.value)}
            placeholder="나이"
            required
          />
          <select
            value={gender}
            onChange={(e) => setGender(e.target.value)}
            required
          >
            <option value="">성별 선택</option>
            <option value="male">남성</option>
            <option value="female">여성</option>
            <option value="other">기타</option>
          </select>
          <button type="submit">분석</button>
        </form>
        {error && <p>{error}</p>}
      </main>
    </div>
  );
}

export default App;

