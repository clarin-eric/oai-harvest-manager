from dataclasses import dataclass, field
from typing import List, Optional


@dataclass
class Provider:
    name: str
    profile: Optional[str] = field(default=None)
    level: Optional[int] = field(default=None)
    default: Optional[bool] = field(default=None)

    def __str__(self):
        return f"Provider(\n{self.name=},\n {self.profile=},\n {self.level=},\n {self.default=})"


@dataclass
class Providers:
    default: bool = field(default=False)
    provider_keys: List[str] = field(default_factory=list)
    providers: List[Provider] = field(default_factory=list)

    def __str__(self):
        return f"Providers({self.default=}, {self.providers=})"

    def get_provider(self, name: str | None) -> Optional[Provider]:
        """
        Get a provider by name.
        """
        if name is None:
            return None
        for provider in self.providers:
            if provider.name == name:
                return provider
        return None
