package com.boardly.infrastructure.adapters.out.persistence.workspace;

import com.boardly.features.workspace.domain.Workspace;
import com.boardly.features.workspace.domain.port.WorkspaceRepository;
import com.boardly.shared.common.value.UserId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WorkspaceRepositoryImpl implements WorkspaceRepository {

    private final WorkspaceJpaRepository workspaceJpaRepository;

    @Override
    public List<Workspace> findByUserId(UserId userId) {
        return workspaceJpaRepository
            .findAllAccessibleByUser(userId.getId())
            .stream()
            .map(WorkspaceEntity::toDomainEntity)
            .toList();
    }
}
