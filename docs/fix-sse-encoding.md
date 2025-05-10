# SSE中文乱码修复记录

## 问题描述
AI助手回复中文出现乱码

## 原因
TextDecoder未使用stream:true，多字节字符在网络分片边界被截断

## 修复
使用buffer拼接 + decoder.decode(value, { stream: true })

## 修复人
李倩
## 日期
2025-05-10
