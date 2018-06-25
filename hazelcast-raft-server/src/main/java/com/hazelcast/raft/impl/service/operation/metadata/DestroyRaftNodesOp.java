/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.raft.impl.service.operation.metadata;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.raft.RaftGroupId;
import com.hazelcast.raft.impl.RaftNode;
import com.hazelcast.raft.impl.RaftOp;
import com.hazelcast.raft.impl.RaftSystemOperation;
import com.hazelcast.raft.impl.service.RaftService;
import com.hazelcast.raft.impl.service.RaftServiceDataSerializerHook;
import com.hazelcast.spi.Operation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * When a Raft group is destroyed, its members terminate their internal {@link RaftNode} instances.
 * This operation is sent to members of destroyed Raft groups.
 * <p/>
 * Please note that this operation is not a {@link RaftOp}, so it is not handled via the Raft layer.
 */
public class DestroyRaftNodesOp extends Operation implements IdentifiedDataSerializable, RaftSystemOperation {

    private Collection<RaftGroupId> groupIds;

    public DestroyRaftNodesOp() {
    }

    public DestroyRaftNodesOp(Collection<RaftGroupId> groupIds) {
        this.groupIds = groupIds;
    }

    @Override
    public void run() {
        RaftService service = getService();
        for (RaftGroupId groupId : groupIds) {
            service.destroyRaftNode(groupId);
        }
    }

    @Override
    public String getServiceName() {
        return RaftService.SERVICE_NAME;
    }

    @Override
    public boolean returnsResponse() {
        return false;
    }

    @Override
    public int getFactoryId() {
        return RaftServiceDataSerializerHook.F_ID;
    }

    @Override
    public int getId() {
        return RaftServiceDataSerializerHook.DESTROY_RAFT_NODES_OP;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeInt(groupIds.size());
        for (RaftGroupId groupId : groupIds) {
            out.writeObject(groupId);
        }
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        int count = in.readInt();
        groupIds = new ArrayList<RaftGroupId>();
        for (int i = 0; i < count; i++) {
            RaftGroupId groupId = in.readObject();
            groupIds.add(groupId);
        }
    }

    @Override
    protected void toString(StringBuilder sb) {
        sb.append(", groupIds=").append(groupIds);
    }
}
