package com.agriverse.api.search.dto;

import java.util.List;

public record AutocompleteResponse(List<AutocompleteSuggestion> suggestions) {
}
