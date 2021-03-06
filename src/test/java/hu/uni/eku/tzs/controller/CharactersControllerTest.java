package hu.uni.eku.tzs.controller;

import hu.uni.eku.tzs.controller.dto.CharactersDto;
import hu.uni.eku.tzs.controller.dto.CharactersMapper;
import hu.uni.eku.tzs.model.Characters;
import hu.uni.eku.tzs.service.CharacterManager;
import hu.uni.eku.tzs.service.exceptions.CharacterAlreadyExistsException;
import hu.uni.eku.tzs.service.exceptions.CharacterNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;


@ExtendWith(MockitoExtension.class)
public class CharactersControllerTest {
    @Mock
    private CharacterManager characterManager;

    @Mock
    private CharactersMapper characterMapper;

    @InjectMocks
    private CharactersController controller;

    @Test
    void readAllHappyPath() { //Read all
        // given
        when(characterManager.readAll()).thenReturn(List.of(TestDataProvider.getTestCharacter()));
        when(characterMapper.characters2charactersDto(any())).thenReturn(TestDataProvider.getTestDto());
        Collection<CharactersDto> expected = List.of(TestDataProvider.getTestDto());
        // when
        Collection<CharactersDto> actual = controller.readAllCharacters();
        //then
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void createCharactersHappyPath() throws CharacterAlreadyExistsException { //Create Character
        // given
        Characters test = TestDataProvider.getTestCharacter();
        CharactersDto testDto = TestDataProvider.getTestDto();
        when(characterMapper.charactersDto2characters(testDto)).thenReturn(test);
        when(characterManager.record(test)).thenReturn(test);
        when(characterMapper.characters2charactersDto(test)).thenReturn(testDto);
        // when
        CharactersDto actual = controller.create(testDto);
        // then
        assertThat(actual).usingRecursiveComparison().isEqualTo(testDto);
    }

    @Test
    void createCharacterAlreadyExistsException() throws CharacterAlreadyExistsException {//Create character already exists exception
        // given
        Characters test = TestDataProvider.getTestCharacter();
        CharactersDto testDto = TestDataProvider.getTestDto();
        when(characterMapper.charactersDto2characters(testDto)).thenReturn(test);
        when(characterManager.record(test)).thenThrow(new CharacterAlreadyExistsException());
        // when then
        assertThatThrownBy(() -> {
            controller.create(testDto);
        }).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void updateHappyPath() throws CharacterNotFoundException { //Update character
        // given
        CharactersDto requestDto = TestDataProvider.getTestDto();
        Characters test = TestDataProvider.getTestCharacter();
        when(characterMapper.charactersDto2characters(requestDto)).thenReturn(test);
        when(characterManager.modify(test)).thenReturn(test);
        when(characterMapper.characters2charactersDto(test)).thenReturn(requestDto);
        CharactersDto expected = TestDataProvider.getTestDto();
        // when
        CharactersDto response = controller.modify(requestDto);
        // then
        assertThat(response).usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void deleteFromQueryParamHappyPath() throws CharacterNotFoundException { //Delete character
        // given
        Characters test = TestDataProvider.getTestCharacter();
        when(characterManager.readById(TestDataProvider.ID)).thenReturn(test);
        doNothing().when(characterManager).delete(test);
        // when
        controller.delete(TestDataProvider.ID);
        // then is not necessary, mock are checked by default
    }

    @Test
    void deleteFromQueryParamWhenCharacterNotFound() throws CharacterNotFoundException { //Delete character character not found
        // given
        final int notFoundCharacterID = TestDataProvider.ID;
        doThrow(new CharacterNotFoundException()).when(characterManager).readById(notFoundCharacterID);
//        These two lines mean the same.
//        doThrow(new BookNotFoundException()).when(bookManager).readByIsbn(notFoundBookIsbn);
//        when(bookManager.readByIsbn(notFoundBookIsbn)).thenThrow(new BookNotFoundException());
        // when then
        assertThatThrownBy(() -> controller.delete(notFoundCharacterID))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void readByIdHappyPath() throws CharacterNotFoundException {
        when(characterManager.readById(TestDataProvider.getTestCharacter().getId())).thenReturn(TestDataProvider.getTestCharacter());
        CharactersDto expected = TestDataProvider.getTestDto();
        when(characterMapper.characters2charactersDto(any())).thenReturn(TestDataProvider.getTestDto());
        CharactersDto actual = controller.readById(TestDataProvider.getTestCharacter().getId());
        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void readByIdCharacterNotFoundException() throws CharacterNotFoundException {
        final int notFoundCharacterId = TestDataProvider.ID;
        doThrow(new CharacterNotFoundException()).when(characterManager).readById(notFoundCharacterId);
        assertThatThrownBy(() -> controller.readById(notFoundCharacterId))
                .isInstanceOf(ResponseStatusException.class);
    }

    private static class TestDataProvider {

        public static final int ID = 1;

        public static Characters getTestCharacter() {
            return new Characters(ID, "Johnny Test", "Johnny", "A good test subject is hard to find");
        }

        public static CharactersDto getTestDto() {
            return CharactersDto.builder()
                    .id(ID)
                    .charName("Johnny Test")
                    .abbreviation("Johnny")
                    .description("A good test subject is hard to find")
                    .build();
        }
    }
}
