// src/app/api/receive-location/route.ts
import { NextRequest, NextResponse } from 'next/server'

export async function POST(req: NextRequest) {
  const { city, district } = await req.json()
  console.log("📥 받은 위치:", city, district)

  // 여기서 8080 서버로 동 요청
  const dongRes = await fetch(`http://localhost:8080/api/location/dongList?city=${city}&district=${district}`)
  const dongList = await dongRes.json()

  return NextResponse.json({ dongs: dongList }) // 클라이언트로 반환
}
