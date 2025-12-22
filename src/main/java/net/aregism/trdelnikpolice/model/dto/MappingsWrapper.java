package net.aregism.trdelnikpolice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.aregism.trdelnikpolice.model.common.Mapping;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MappingsWrapper {
    private List<Mapping> record;
}
