package net.aregism.trdelnikpolice.model.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.aregism.trdelnikpolice.model.common.Position;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mapping {
    private String keyword;
    private List<String> responses = new ArrayList<>();
    private Position position = Position.LAST;
}
