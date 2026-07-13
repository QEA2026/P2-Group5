from dataclasses import dataclass
from typing import Optional

@dataclass
class User:
    user_id: Optional[int]
    username: str
    password: str
    role: str

    def __post_init__(self):
        if self.role != 'employee':
            raise ValueError("Role must be 'employee'")