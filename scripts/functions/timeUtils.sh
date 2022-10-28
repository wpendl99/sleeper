# Copyright 2022 Crown Copyright
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

source "$(dirname "${BASH_SOURCE[0]}")/stringUtils.sh"

time_str() {
  date -u +"$(time_format)"
}

record_time() {
  date +"%s"
}

if date -d @0 &>/dev/null; then
  recorded_time_str() {
    date -ud "@$1" +"$(time_format)"
  }
else
  recorded_time_str() {
    date -ur "$1" +"$(time_format)"
  }
fi

time_format() {
  echo "%Y-%m-%d %T UTC"
}

elapsed_time_str() {
  local START=$1
  local END=$2
  local SECONDS=$((END-START))
  seconds_to_str ${SECONDS}
}
