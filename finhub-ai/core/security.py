from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from jose import JWTError, jwt
from core.config import settings

bearer_scheme = HTTPBearer(auto_error=False)

# JJWT Keys.hmacShaKeyFor() + signWith(key) 알고리즘 자동 선택 규칙:
#   key >= 512bit → HS512 / key >= 384bit → HS384 / key >= 256bit → HS256
# secret "finhub-secret-key-must-be-at-least-256-bits-long-for-hs256" = 58bytes = 464bit → HS384
_ALGORITHMS = ["HS384", "HS256", "HS512"]


def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(bearer_scheme),
) -> dict:
    if credentials is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="인증 토큰이 필요합니다.",
        )

    token = credentials.credentials

    for algorithm in _ALGORITHMS:
        try:
            payload = jwt.decode(
                token,
                settings.JWT_SECRET,
                algorithms=[algorithm],
                options={"verify_exp": False},
            )
            print(f"[JWT] 알고리즘 {algorithm} 성공")
            user_id: str = str(payload.get("userId") or payload.get("sub") or "")
            if not user_id:
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="유효하지 않은 토큰입니다.",
                )
            return {"user_id": user_id, "email": payload.get("sub"), "payload": payload}
        except JWTError as e:
            print(f"[JWT] 실패: {str(e)}")
            continue

    raise HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="유효하지 않은 토큰입니다.",
    )
