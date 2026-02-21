import axios from "axios";

export const http = axios.create({
  baseURL: process.env.API_URL,
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
  },
});
